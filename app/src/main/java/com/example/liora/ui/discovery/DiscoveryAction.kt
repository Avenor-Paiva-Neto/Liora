package com.example.liora.ui.discovery

sealed class DiscoveryAction {
    data object Like : DiscoveryAction()
    data object Skip : DiscoveryAction()
    data object Undo : DiscoveryAction()
    data object Report : DiscoveryAction()
    data object Chat : DiscoveryAction()
    data object Retry : DiscoveryAction()
    data object BioToggle : DiscoveryAction()
}


