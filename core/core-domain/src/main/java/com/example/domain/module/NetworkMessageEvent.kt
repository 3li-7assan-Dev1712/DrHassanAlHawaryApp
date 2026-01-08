package com.example.domain.module

// Sealed class for specific network messages (one-time events)
sealed class NetworkMessageEvent {
    object WentOffline : NetworkMessageEvent()
    object BackOnline : NetworkMessageEvent()
}