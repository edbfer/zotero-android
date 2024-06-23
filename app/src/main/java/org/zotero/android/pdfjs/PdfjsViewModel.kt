package org.zotero.android.pdfjs

import android.content.Context
import android.graphics.RectF
import android.net.Uri
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.commit
import androidx.lifecycle.SavedStateHandle
import com.pspdfkit.annotations.SquareAnnotation
import com.pspdfkit.ui.special_mode.manager.AnnotationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.OrderedCollectionChangeSet
import io.realm.RealmResults
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.internal.userAgent
import org.greenrobot.eventbus.EventBus
import org.jetbrains.kotlin.tooling.core.compareTo
import org.jetbrains.kotlin.tooling.core.extrasKeyOf
import org.zotero.android.api.network.CustomResult
import org.zotero.android.architecture.BaseViewModel2
import org.zotero.android.architecture.Defaults
import org.zotero.android.architecture.ScreenArguments
import org.zotero.android.architecture.ViewEffect
import org.zotero.android.architecture.ViewState
import org.zotero.android.architecture.navigation.NavigationParamsMarshaller
import org.zotero.android.architecture.require
import org.zotero.android.database.Database
import org.zotero.android.database.DbWrapper
import org.zotero.android.database.objects.AnnotationType
import org.zotero.android.database.objects.AnnotationsConfig
import org.zotero.android.database.objects.RItem
import org.zotero.android.database.requests.ReadAnnotationsDbRequest
import org.zotero.android.database.requests.ReadDocumentDataDbRequest
import org.zotero.android.database.requests.key
import org.zotero.android.pdf.annotation.data.PdfAnnotationArgs
import org.zotero.android.pdf.annotationmore.data.PdfAnnotationMoreArgs
import org.zotero.android.pdf.cache.AnnotationPreviewFileCache
import org.zotero.android.pdf.cache.AnnotationPreviewMemoryCache
import org.zotero.android.pdf.data.Annotation
import org.zotero.android.pdf.data.AnnotationBoundingBoxConverter
import org.zotero.android.pdf.data.AnnotationPreviewManager
import org.zotero.android.pdf.data.AnnotationsFilter
import org.zotero.android.pdf.data.DatabaseAnnotation
import org.zotero.android.pdf.data.DocumentAnnotation
import org.zotero.android.pdf.reader.AnnotationKey
import org.zotero.android.pdf.reader.PdfReaderViewEffect
import org.zotero.android.pdffilter.data.PdfFilterArgs
import org.zotero.android.pdfjs.data.PdfjsAnnotation
import org.zotero.android.pdfjs.data.PdfjsAnnotationBoundingBoxConverter
import org.zotero.android.screens.libraries.LibrariesViewModel
import org.zotero.android.screens.tagpicker.data.TagPickerArgs
import org.zotero.android.screens.tagpicker.data.TagPickerResult
import org.zotero.android.sync.AnnotationConverter
import org.zotero.android.sync.Library
import org.zotero.android.sync.LibraryIdentifier
import org.zotero.android.sync.SessionDataEventStream
import org.zotero.android.sync.Tag
import org.zotero.android.uicomponents.modal.ModalDialog
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.atan

@HiltViewModel
class PdfjsViewModel @Inject constructor(
    private val defaults: Defaults,
    private val dbWrapper: DbWrapper,
    private val sessionDataEventStream: SessionDataEventStream,
    private val context: Context,
    private val navigationParamsMarshaller: NavigationParamsMarshaller,
    val annotationPreviewMemoryCache: AnnotationPreviewMemoryCache,
    private val fileCache: AnnotationPreviewFileCache,
    private val annotationPreviewManager: AnnotationPreviewManager,
    stateHandle: SavedStateHandle
) : BaseViewModel2<PdfjsViewState, PdfjsViewEffect>(PdfjsViewState()) {

    private var isTablet: Boolean = false
    private var databaseAnnotations: RealmResults<RItem>? = null
    private var containerId = 0
    private val onCommentChangeFlow = MutableStateFlow<Pair<String, String>?>(null)
    private val onSearchStateFlow = MutableStateFlow("")
    private var liveAnnotations: RealmResults<RItem>? = null
    private lateinit var document: PdfjsDocument
    private lateinit var rawDocument: PdfjsDocument
    private lateinit var fragment : PdfjsFragment
    private lateinit var fragmentManager: FragmentManager
    private lateinit var annotationBoundingBoxConverter: PdfjsAnnotationBoundingBoxConverter

    private var requested_uri: String = ""
    var annotationMaxSideSize = 0

    val selectedAnnotation: org.zotero.android.pdf.data.Annotation?
        get()
        {
            return viewState.selectedAnnotationKey?.let {annotation(it)}
        }

    val screenArgs: PdfjsReaderArgs by lazy {
        val argsEncoded = stateHandle.get<String>("pdfjsScreenArgs").require()
        navigationParamsMarshaller.decodeObjectFromBase64(argsEncoded)
    }

    fun selectAnnotation(key: AnnotationKey)
    {
        if(!viewState.sidebarEditingEnabled && key != viewState.selectedAnnotationKey)
            _select(key = key, didSelectInDocument = false)
    }

    fun annotation(key: AnnotationKey) : org.zotero.android.pdf.data.Annotation?
    {
        when(key.type)
        {
            AnnotationKey.Kind.database -> {
                return this.databaseAnnotations!!.where().key(key.key).findFirst()
                    ?.let{ DatabaseAnnotation(item = it) }
            }

            AnnotationKey.Kind.document ->
            {
                return viewState.documentAnnotations[key.key]
            }
        }
    }

    fun onStop(isChangingConfigurations: Boolean)
    {

    }

    fun selectAnnotationFromDocument(key: AnnotationKey)
    {
        if(!viewState.sidebarEditingEnabled && key != viewState.selectedAnnotationKey)
        {
            _select(key = key, didSelectInDocument = true)
        }
    }

    fun onCommentFocusFieldChange(annotationKey: String)
    {
        val key = AnnotationKey(key = annotationKey, type = AnnotationKey.Kind.database)
        val annotation = annotation(key)?: return
        selectAnnotationFromDocument(key)

        updateState {
            copy(commentFocusKey = annotationKey, commentFocusText = annotation.comment)
        }
    }

    fun onMoreOptionsForItemClicked()
    {
        ScreenArguments.pdfAnnotationMoreArgs = PdfAnnotationMoreArgs(
            selectedAnnotation = selectedAnnotation,
            userId = viewState.userId,
            library = viewState.library
        )
        triggerEffect(PdfjsViewEffect.ShowPdfAnnotationMore)
    }

    fun toggleSideBar()
    {
        updateState {
            copy(showSideBar = !showSideBar)
        }
    }

    fun init(
        path: String,
        annotationMaxSideSize: Int,
        isTablet : Boolean,
        containerId: Int,
        fragmentManager: FragmentManager,
    )
    {
        this.isTablet = isTablet
        this.containerId = containerId
        this.fragmentManager = fragmentManager
        this.requested_uri = path

        if(this::fragment.isInitialized)
        {
            replaceFragment()
            return
        }
        this@PdfjsViewModel.fragment = PdfjsFragment(path = requested_uri, onDocumentLoadedCallback = this::onDocumentLoaded)

        //EventBus.getDefault().register(this)

        initState()
        fragmentManager.commit {
            add(containerId, this@PdfjsViewModel.fragment)
        }
    }

    fun loadPreviews(keys: List<String>)
    {
        if (keys.isEmpty()) else {
            return
        }

        val isDark = viewState.isDark
        val libraryId = viewState.library.identifier

        for(key in keys)
        {
            if(annotationPreviewMemoryCache.getBitmap(key) != null)
            {
                continue
            }
            fileCache.preview(
                key = key,
                parentKey = viewState.key,
                libraryId = libraryId,
                isDark = isDark
            )
        }
    }

    fun showFilterPopup()
    {
        val colors = mutableSetOf<String>()
        val tags = mutableSetOf<Tag>()

        val processAnnotation: (org.zotero.android.pdf.data.Annotation) -> Unit =
            {annotation ->
                colors.add(annotation.color)
                for(tag in annotation.tags)
                {
                    tags.add(tag)
                }
            }

        for (annotation in this.databaseAnnotations!!)
            processAnnotation(DatabaseAnnotation(item = annotation))

        for (annotation in this.viewState.documentAnnotations.values)
            processAnnotation(annotation)

        val sortedTags = tags.sortedWith { lTag, rTag ->
            if (lTag.color.isEmpty() == rTag.color.isEmpty())
                return@sortedWith lTag.name.compareTo(other = rTag.name, ignoreCase = true)

            if (!lTag.color.isEmpty() && rTag.color.isEmpty())
                return@sortedWith 1

            -1
        }

        val sortedColors = mutableListOf<String>()
        AnnotationsConfig.allColors.forEach { color ->
            if (colors.contains(color))
                sortedColors.add(color)
        }
        ScreenArguments.pdfFilterArgs = PdfFilterArgs(
            filter = viewState.filter,
            availableColors = sortedColors,
            availableTags = sortedTags
        )
        triggerEffect(PdfjsViewEffect.ShowPdfFilters)
    }

    fun onCommentTextChange(annotationKey: String, comment: String)
    {
        updateState {
            copy(commentFocusText = comment)
        }
        onCommentChangeFlow.tryEmit(annotationKey to comment)
    }

    fun onTagsClicked(annotation: org.zotero.android.pdf.data.Annotation)
    {
        val annotationKey = AnnotationKey(key = annotation.key, type = AnnotationKey.Kind.database)
        selectAnnotationFromDocument(annotationKey)

        val selected = annotation.tags.map { it.name }.toSet()

        ScreenArguments.tagPickerArgs = TagPickerArgs(
            libraryId = viewState.library.identifier,
            selectedTags = selected,
            tags = emptyList(),
            callPoint = TagPickerResult.CallPoint.PdfReaderScreen
        )

        triggerEffect(PdfjsViewEffect.NavigateToTagPickerScreen)
    }

    fun onSearch(text: String)
    {
        updateState {
            copy(searchTerm = text)
        }
        onSearchStateFlow.tryEmit(text)
    }

    fun onDocumentLoaded(document: PdfjsDocument)
    {
        this.document = document
        annotationBoundingBoxConverter = PdfjsAnnotationBoundingBoxConverter(document)
        loadRawDocument()
        loadDocumentData()
        setupInteractionListeners()
    }

    private fun loadRawDocument()
    {
        this.rawDocument = PdfjsDocument.loadDocument(context, this.requested_uri)
    }

    private fun loadDocumentData()
    {
        val key = viewState.key
        val library = viewState.library
        val dbResult = loadAnnotationsAndPage(key = key, library = library)

        when (dbResult)
        {
            is CustomResult.GeneralSuccess ->
            {
                this.liveAnnotations?.removeAllChangeListeners()
                this.liveAnnotations = dbResult.value!!.first
                val storedPage = dbResult.value!!.second
                observe(liveAnnotations!!)
                this.databaseAnnotations = liveAnnotations!!.freeze()
                val documentAnnotations = loadAnnotations(
                    this.document,
                    username = viewState.username,
                    displayName = viewState.displayName
                )
                val dbToPdfAnnotations = AnnotationConverter.annotations(
                    this.databaseAnnotations!!,
                    isDarkMode = false,
                    currentUserId = viewState.userId,
                    library = library,
                    displayName = viewState.displayName,
                    username = viewState.username,
                    boundingBoxConverter = annotationBoundingBoxConverter
                )
                val sortedKeys = createSortedKeys(
                    databaseAnnotations = databaseAnnotations!!,
                    documentAnnotations = documentAnnotations
                )

                update(
                    document = this.document,
                    zoteroAnnotations = dbToPdfAnnotations,
                    key = key,
                    libraryId = library.identifier,
                    isDark = viewState.isDark
                )
                for (annotation in dbToPdfAnnotations)
                {
                    annotationPreviewManager.store(
                        rawDocument = PdfjsDocument,
                        annotation = annotation,
                        parentKey = key,
                        libraryId = library.identifier,
                        isDark = viewState.isDark,
                        annotationMaxSideSize = annotationMaxSideSize
                    )
                }

                val (page, selectedData) = preselectedData(
                    databaseAnnotations = databaseAnnotations!!,
                    storedPage = storedPage,
                    boundingBoxConverter = annotationBoundingBoxConverter
                )

                updateState {
                    copy(
                        documentAnnotations = documentAnnotations,
                        sortedKeys = sortedKeys,
                        visiblePage = page,
                        initialPage = null
                    )
                }

                this.fragment.pageIndex = page

                if (selectedData != null)
                {
                    val (key, location) = selectedData
                    updateState {
                        copy(
                            selectedAnnotationKey = key,
                            focusDocumentLocation = location,
                            focusSidebarKey = key
                        )
                    }
                }
            }

            is CustomResult.GeneralError.CodeError ->
            {
                Timber.e(dbResult.throwable)
            }

            else -> {}
        }
        observeDocument()
        updateAnnotationsList(forceNotShowAnnotationPopup = true)
    }

    private fun setupInteractionListeners()
    {
        //TODO: Annotation Interface
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun loadAnnotations(
        document: PdfjsDocument,
        username: String,
        displayName: String
    ) : Map<String, DocumentAnnotation>
    {
        val annotations = mutableMapOf<String, DocumentAnnotation>()
        val pdfAnnotations = document.annotationProvider.getAllAnnotations()

        for (pdfAnnotation in pdfAnnotations)
        {
            //TODO: WAS ANIMATION CONVERTER HERE
            annotations[pdfAnnotation.key] = pdfAnnotation
        }
        return annotations
    }

    private fun createSortedKeys(
        databaseAnnotations: RealmResults<RItem>,
        documentAnnotations: Map<String, DocumentAnnotation>
    ) : List<AnnotationKey>
    {
        val sortMap = mapOf<String, AnnotationKey>().toSortedMap()
        for (item in databaseAnnotations)
        {
            if(!validate(databaseAnnotation = DatabaseAnnotation(item = item)))
                continue

            sortMap[item.annotationSortIndex] = AnnotationKey(
                key = item.key,
                type = AnnotationKey.Kind.database
            )
        }
        for (annotation in documentAnnotations.values)
        {
            val key = AnnotationKey(key = annotation.key, type = AnnotationKey.Kind.document)
            val sortIndex = annotation.sortIndex
            sortMap[sortIndex] = key
        }
        val result = sortMap.map { it.value }
        return result
    }

    private fun validate(
        databaseAnnotation: DatabaseAnnotation
    ) : Boolean
    {
        if (databaseAnnotation._page == null)
            return false

        when(databaseAnnotation.type)
        {
            org.zotero.android.database.objects.AnnotationType.ink ->
            {
                if(databaseAnnotation.item.paths.isEmpty())
                {
                    Timber.i("PdfjsViewModel: ink annotation ${databaseAnnotation.key} missing paths")
                    return false
                }
            }

            org.zotero.android.database.objects.AnnotationType.note,
            org.zotero.android.database.objects.AnnotationType.highlight,
            org.zotero.android.database.objects.AnnotationType.image ->
            {
                if (databaseAnnotation.item.rects.isEmpty())
                {
                    Timber.i("PdfjsViewModel: ${databaseAnnotation.type} annotation ${databaseAnnotation.key} missing rects")
                    return false
                }
            }
        }

        val sortIndex = databaseAnnotation.sortIndex
        val parts = sortIndex.split("|")
        if (parts.size != 3 || parts[0].length != 5 || parts[1].length != 6 || parts[2].length != 5)
        {
            Timber.i("PdfjsViewModel: invalid sort index (${sortIndex}) for ${databaseAnnotation.key}")
            return false
        }

        return true
    }

    private fun update(
        document: PdfjsDocument,
        zoteroAnnotations: List<PdfjsAnnotation>,
        key: String,
        libraryId: LibraryIdentifier,
        isDark: Boolean
    )
    {
        val allAnnotations = document.annotationProvider.getAllAnnotations()

        for (annotation in allAnnotations)
        {
            //TODO: annotation flags
            annotationPreviewManager.store(
                rawDocument = this.rawDocument,
                annotation = annotation,
                parentKey = key,
                libraryId = libraryId,
                isDark = isDark,
                annotationMaxSideSize = annotationMaxSideSize
            )
        }
        zoteroAnnotations.forEach {
            document.annotationProvider.addAnnotationToPage(it)
        }
    }

    private fun loadAnnotationsAndPage(
        key: String,
        library: Library
    ): CustomResult<Pair<RealmResults<RItem>, Int>>
    {
        try
        {
            var pageStr = "0"
            var results: RealmResults<RItem>? = null
            dbWrapper.realmDbStorage.perform { coordinator->
                pageStr = coordinator.perform(
                    request = ReadDocumentDataDbRequest(
                        attachmentKey = key,
                        libraryId = library.identifier
                    )
                )
                results = coordinator.perform(
                    request = ReadAnnotationsDbRequest(
                        attachmentKey = key,
                        libraryId = library.identifier
                    )
                )
            }
            val page = pageStr.toIntOrNull()
            if (page == null)
                return CustomResult.GeneralError.CodeError(Exception("Can't add annotations"))

            return CustomResult.GeneralSuccess(results!! to page)
        }
        catch (e: Exception)
        {
            Timber.e(e)
            return CustomResult.GeneralError.CodeError(e)
        }
    }

    private fun replaceFragment()
    {
        this.fragment = PdfjsFragment(path = requested_uri, onDocumentLoadedCallback = this::onDocumentLoaded)
    }

    private fun selectAndFocusAnnotationInDocument()
    {
        val annotation = this.selectedAnnotation
        if(annotation != null)
        {
            val location = viewState.focusDocumentLocation
            if(location != null)
            {
                focus(annotation = annotation, location = location, document = this.document)
            }
            else if (annotation.type != org.zotero.android.database.objects.AnnotationType.ink /*|| fragment.activeAnnotationTool?.toAnnotationType() != AnnotationType.INK*/)
            {
                //TODO: annotation interface
                val pageIndex = fragment.pageIndex
                select(annotation = annotation, pageIndex = pageIndex, document = this.document)
            }
        }
        else
        {
            select(annotation = null, pageIndex = fragment.pageIndex, document = this.document)
        }
    }

    private fun scrollIfNeeded(pageIndex: Int, animated: Boolean, completion: () -> Unit)
    {
        if(fragment.pageIndex == pageIndex)
        {
            completion()
            return
        }

        fragment.setPageIndex(pageIndex, animated)
        completion()
    }

    private fun observe(results: RealmResults<RItem>)
    {
        results.addChangeListener { objects, changeSet ->
            when(changeSet.state)
            {
                OrderedCollectionChangeSet.State.INITIAL -> {}
                OrderedCollectionChangeSet.State.UPDATE ->
                {
                    val deletions = changeSet.deletions
                    val modifications = changeSet.changes
                    val insertions = changeSet.insertions
                    update(
                        objects = objects,
                        deletions = deletions,
                        insertions = insertions,
                        modifications = modifications
                    )
                }

                OrderedCollectionChangeSet.State.ERROR ->
                {
                    Timber.e(changeSet.error, "PdfReaderViewModel: could not load results")
                }

                else -> {}
            }
        }
    }

    private fun focus(
        annotation: org.zotero.android.pdf.data.Annotation,
        location: Pair<Int, RectF>,
        document: PdfjsDocument
    )
    {
        val pageIndex = annotation.page
        scrollIfNeeded(pageIndex, true)
        {
            select(annotation = annotation, pageIndex = pageIndex, document = document)
        }
    }

    private fun updateAnnotationsList(forceNotShowAnnotationPopup: Boolean = false)
    {
        val showAnnotationPopup = !forceNotShowAnnotationPopup && !viewState.showSideBar && selectedAnnotation != null
        if(showAnnotationPopup)
        {
            ScreenArguments.pdfAnnotationArgs = PdfAnnotationArgs(
                selectedAnnotation = selectedAnnotation,
                userId = viewState.userId,
                library =  viewState.library
            )
        }

        val index = viewState.sortedKeys.indexOf(viewState.selectedAnnotationKey)
        triggerEffect(
            PdfjsViewEffect.ShowPdfAnnotationAndUpdateAnnotationsList(
                index, showAnnotationPopup
            )
        )
    }

    private fun select(
        annotation: org.zotero.android.pdf.data.Annotation?,
        pageIndex: Int,
        document: PdfjsDocument
    )
    {
        if(annotation != null)
        {
            val pdfAnnotation = document.annotation(pageIndex, annotation.key)
            if(pdfAnnotation != null)
            {
                if(!fragment.selectedAnnotations.contains(pdfAnnotation))
                {
                    fragment.setSelectedAnnotation(pdfAnnotation)
                    val zoomScale = fragment.getZoomScale(pageIndex)
                    if(zoomScale > 1.0)
                    {
                        fragment.scrollTo(pdfAnnotation.boundingBox, pageIndex, 100, false)
                    }
                }
            }
            else
            {
                if(!fragment.selectedAnnotations.isEmpty())
                {
                    fragment.clearSelectedAnnotations()
                }
            }
        }
        else
        {
            if(!fragment.selectedAnnotations.isEmpty())
                fragment.clearSelectedAnnotations()
        }
    }

    private fun _select(key: AnnotationKey?, didSelectInDocument: Boolean)
    {
        if(key == viewState.selectedAnnotationKey)
            return

        val existing = viewState.selectedAnnotationKey
        if(existing != null)
        {
            if(viewState.sortedKeys.contains(existing))
            {
                val updatedAnnotationKeys = (viewState.updatedAnnotationKeys ?: emptyList()).toMutableList()
                updatedAnnotationKeys.add(existing)
                updateState {
                    copy(updatedAnnotationKeys = updatedAnnotationKeys)
                }
            }

            if(viewState.selectedAnnotationCommentActive)
            {
                updateState {
                    copy(selectedAnnotationCommentActive = false)
                }
            }
        }

        if(key == null)
        {
            updateState {
                copy(selectedAnnotationKey = null)
            }
            selectAndFocusAnnotationInDocument()
            updateAnnotationsList()
            return
        }

        updateState {
            copy(selectedAnnotationKey = key)
        }

        if(!didSelectInDocument)
        {
            val annotation = annotation(key)
            if(annotation != null)
            {
                updateState {
                    copy(
                        focusDocumentLocation = (annotation.page to annotation.boundingBox(
                            boundingBoxConverter = annotationBoundingBoxConverter
                        ))
                    )
                }
            }
        } else {
            updateState {
                copy(focusSidebarKey = key)
            }
        }

        if(viewState.sortedKeys.contains(key))
        {
            val updatedAnnotationKeys = (viewState.updatedAnnotationKeys ?: emptyList()).toMutableList()
            updatedAnnotationKeys.add(key)
            updateState {
                copy(updatedAnnotationKeys = updatedAnnotationKeys)
            }
        }
        selectAndFocusAnnotationInDocument()
        updateAnnotationsList()
    }

    private fun initState()
    {
        val params = this.screenArgs
        val username = defaults.getUsername()
        val userId = sessionDataEventStream.currentValue()!!.userId
        val displayName = defaults.getDisplayName()

        updateState {
            copy(
                key = params.key,
                library = params.library,
                userId = userId,
                username = username,
                selectedAnnotationKey = params.preselectedAnnotationKey?.let {
                    AnnotationKey(
                        key = it,
                        type = AnnotationKey.Kind.database
                    )
                },
                requested_path = requested_uri
            )
        }
    }

}

data class PdfjsViewState(
    val userId: Long = -1L,
    val username: String = "",
    val displayName: String = "",
    val library: Library = Library(
        identifier = LibraryIdentifier.group(0),
        name = "",
        metadataEditable = false,
        filesEditable = false
    ),
    val key: String = "",
    val visiblePage: Int = 0,
    val initialPage: Int? = null,
    val requested_path: String = "",
    val searchTerm: String = "",
    val focusSidebarKey: AnnotationKey? = null,
    val focusDocumentLocation: Pair<Int, RectF>? = null,
    val isDark: Boolean = false,
    val isTopBarVisible: Boolean = true,
    val sidebarEditingEnabled: Boolean = false,
    var selectedAnnotationCommentActive: Boolean = false,
    val selectedAnnotationKey: AnnotationKey? = null,
    val sortedKeys: List<AnnotationKey> = emptyList(),
    val updatedAnnotationKeys: List<AnnotationKey>? = null,
    val filter: AnnotationsFilter? = null,
    val showSideBar: Boolean = false,
    val showCreationToolbar: Boolean = false,
    val commentFocusKey: String? = null,
    val commentFocusText: String = "",
    val documentAnnotations: Map<String, DocumentAnnotation> = emptyMap()
) : ViewState {

    fun isAnnotationSelected(annotationKey: String): Boolean
    {
        return this.selectedAnnotationKey?.key == annotationKey
    }

}

sealed class PdfjsViewEffect : ViewEffect{
    object NavigateBack : PdfjsViewEffect()
    object ShowPdfAnnotationMore: PdfjsViewEffect()
    object ShowPdfFilters: PdfjsViewEffect()
    object NavigateToTagPickerScreen: PdfjsViewEffect()
    data class ShowPdfAnnotationAndUpdateAnnotationsList
        (val scrollToIndex: Int, val showAnnotationPopup: Boolean) : PdfjsViewEffect()
}