package com.example.foodbuddy

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

public class MainPagerAdapter (private val fragmentManager: FragmentManager,
                               private val bundleMessages: Bundle,
                               private val bundleDiscover: Bundle,
                               private val bundleEvents: Bundle,
                               private val currentUser: User) : FragmentPagerAdapter(fragmentManager) {

    private val numItems = 3

    override fun getItem(p0: Int): Fragment {
        when (p0) {
            0 -> {
                val messagesFragment = MessagesFragment()
                bundleMessages.putSerializable("currentUser", currentUser)
                messagesFragment.arguments = bundleMessages
                return messagesFragment
            }
            1 -> {
                val discoverFragment = DiscoverFragment()
                bundleDiscover.putSerializable("currentUser", currentUser)
                discoverFragment.arguments = bundleDiscover
                return discoverFragment
            }
            else -> {
                val eventsFragment = EventsFragment()
                bundleEvents.putSerializable("currentUser", currentUser)
                eventsFragment.arguments = bundleEvents
                return eventsFragment
            }
        }
    }

    override fun getCount(): Int {
        return numItems
    }

}