package org.zotero.android.pdfjs

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import org.zotero.android.architecture.BaseViewModel2
import org.zotero.android.architecture.ViewEffect
import org.zotero.android.architecture.ViewState
import javax.inject.Inject

@HiltViewModel
class PdfjsViewModel @Inject constructor(
    private val context: Context
) : BaseViewModel2<PdfjsViewState, PdfjsViewEffect>(PdfjsViewState()) {

    private var isTablet: Boolean = false
    private var containerId = 0
    private lateinit var fragment : PdfjsFragment
    private lateinit var fragmentManager: FragmentManager

    fun onStop(isChangingConfigurations: Boolean)
    {

    }

    fun init(
        isTablet : Boolean,
        containerId: Int,
        fragmentManager: FragmentManager,
    )
    {
        this.isTablet = isTablet
        this.containerId = containerId
        this.fragmentManager = fragmentManager

        if(this::fragment.isInitialized)
        {
            replaceFragment()
            return
        }
        this@PdfjsViewModel.fragment = PdfjsFragment()

        //EventBus.getDefault().register(this)

        initState()
        fragmentManager.commit {
            add(containerId, this@PdfjsViewModel.fragment)
        }
    }

    private fun replaceFragment()
    {
        this.fragment = PdfjsFragment()
    }

    private fun initState()
    {
        
    }

}

data class PdfjsViewState(
    val key: String = "",
    val isDark: Boolean = false,
    val isTopBarVisible: Boolean = true,
    val showSideBar: Boolean = false,
    val showCreationToolbar: Boolean = false
) : ViewState {}

sealed class PdfjsViewEffect : ViewEffect{
    object NavigateBack : PdfjsViewEffect()
}