package org.zotero.android.pdfjs

import android.content.Context
import android.net.Uri
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import org.zotero.android.architecture.BaseViewModel2
import org.zotero.android.architecture.ViewEffect
import org.zotero.android.architecture.ViewState
import org.zotero.android.architecture.navigation.NavigationParamsMarshaller
import org.zotero.android.architecture.require
import javax.inject.Inject

@HiltViewModel
class PdfjsViewModel @Inject constructor(
    private val context: Context,
    private val navigationParamsMarshaller: NavigationParamsMarshaller,
    stateHandle: SavedStateHandle
) : BaseViewModel2<PdfjsViewState, PdfjsViewEffect>(PdfjsViewState()) {

    private var isTablet: Boolean = false
    private var containerId = 0
    private lateinit var fragment : PdfjsFragment
    private lateinit var fragmentManager: FragmentManager

    private var requested_uri: String = ""

    val screenArgs: PdfjsReaderArgs by lazy {
        val argsEncoded = stateHandle.get<String>("pdfjsScreenArgs").require()
        navigationParamsMarshaller.decodeObjectFromBase64(argsEncoded)
    }

    fun onStop(isChangingConfigurations: Boolean)
    {

    }

    fun toggleSideBar()
    {
        updateState {
            copy(showSideBar = !showSideBar)
        }
    }

    fun init(
        path: String,
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
        this@PdfjsViewModel.fragment = PdfjsFragment(path = requested_uri)

        //EventBus.getDefault().register(this)

        initState()
        fragmentManager.commit {
            add(containerId, this@PdfjsViewModel.fragment)
        }
    }

    private fun replaceFragment()
    {
        this.fragment = PdfjsFragment(path = requested_uri)
    }

    private fun initState()
    {
        updateState {
            copy(
                requested_path = requested_uri
            )
        }
    }

}

data class PdfjsViewState(
    val key: String = "",
    val requested_path: String = "",
    val isDark: Boolean = false,
    val isTopBarVisible: Boolean = true,
    val showSideBar: Boolean = false,
    val showCreationToolbar: Boolean = false
) : ViewState {}

sealed class PdfjsViewEffect : ViewEffect{
    object NavigateBack : PdfjsViewEffect()
}