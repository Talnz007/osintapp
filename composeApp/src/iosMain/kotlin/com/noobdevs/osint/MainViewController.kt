package com.noobdevs.osint
 
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

private class IosViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}

fun MainViewController() = ComposeUIViewController {
    val storeOwner = remember { IosViewModelStoreOwner() }
    CompositionLocalProvider(LocalViewModelStoreOwner provides storeOwner) {
        App()
    }
}
