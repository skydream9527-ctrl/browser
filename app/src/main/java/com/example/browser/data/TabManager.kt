package com.example.browser.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * 标签页管理器 - 管理所有浏览器窗口/标签页
 */
class TabManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val tabs = mutableListOf<Tab>()
    private var activeTabId: String? = null
    
    companion object {
        private const val PREFS_NAME = "browser_tabs"
        private const val KEY_TABS = "tabs"
        private const val KEY_ACTIVE_TAB = "active_tab"
        private const val MAX_TABS = 50 // 最大标签页数量
    }
    
    init {
        loadTabs()
    }
    
    /**
     * 获取所有标签页
     */
    fun getAllTabs(): List<Tab> = tabs.toList()
    
    /**
     * 获取当前活动标签页
     */
    fun getActiveTab(): Tab? {
        return tabs.find { it.id == activeTabId }
    }
    
    /**
     * 获取标签页数量
     */
    fun getTabCount(): Int = tabs.size
    
    /**
     * 创建新标签页
     */
    fun createNewTab(url: String = "", title: String = "新标签页"): Tab {
        // 如果已经达到最大数量，删除最旧的非活动标签页
        if (tabs.size >= MAX_TABS) {
            val oldestTab = tabs.filter { it.id != activeTabId }.minByOrNull { it.createdAt }
            oldestTab?.let { removeTab(it.id) }
        }
        
        // 将当前活动标签页设为非活动
        tabs.forEach { it.isActive = false }
        
        // 创建新标签页
        val newTab = Tab(
            url = url,
            title = title,
            isActive = true
        )
        tabs.add(newTab)
        activeTabId = newTab.id
        
        saveTabs()
        return newTab
    }
    
    /**
     * 切换到指定标签页
     */
    fun switchToTab(tabId: String): Tab? {
        val tab = tabs.find { it.id == tabId }
        if (tab != null) {
            // 将所有标签页设为非活动
            tabs.forEach { it.isActive = false }
            // 激活指定标签页
            tab.isActive = true
            activeTabId = tabId
            saveTabs()
        }
        return tab
    }
    
    /**
     * 删除指定标签页
     */
    fun removeTab(tabId: String): Boolean {
        val tab = tabs.find { it.id == tabId }
        if (tab != null) {
            val wasActive = tab.isActive
            tabs.remove(tab)
            
            // 如果删除的是活动标签页，切换到其他标签页
            if (wasActive && tabs.isNotEmpty()) {
                val newActiveTab = tabs.last()
                newActiveTab.isActive = true
                activeTabId = newActiveTab.id
            } else if (tabs.isEmpty()) {
                activeTabId = null
            }
            
            saveTabs()
            return true
        }
        return false
    }
    
    /**
     * 关闭所有标签页
     */
    fun closeAllTabs() {
        tabs.clear()
        activeTabId = null
        saveTabs()
    }
    
    /**
     * 更新标签页信息
     */
    fun updateTab(tabId: String, title: String? = null, url: String? = null, favicon: String? = null) {
        val tab = tabs.find { it.id == tabId }
        tab?.let {
            title?.let { it1 -> it.title = it1 }
            url?.let { it1 -> it.url = it1 }
            favicon?.let { it1 -> it.favicon = it1 }
            saveTabs()
        }
    }
    
    /**
     * 保存标签页到本地
     */
    private fun saveTabs() {
        val jsonArray = JSONArray()
        tabs.forEach { tab ->
            val jsonObject = JSONObject().apply {
                put("id", tab.id)
                put("title", tab.title)
                put("url", tab.url)
                put("favicon", tab.favicon ?: "")
                put("isActive", tab.isActive)
                put("createdAt", tab.createdAt)
            }
            jsonArray.put(jsonObject)
        }
        
        prefs.edit().apply {
            putString(KEY_TABS, jsonArray.toString())
            putString(KEY_ACTIVE_TAB, activeTabId ?: "")
            apply()
        }
    }
    
    /**
     * 从本地加载标签页
     */
    private fun loadTabs() {
        val tabsJson = prefs.getString(KEY_TABS, null)
        if (tabsJson != null) {
            try {
                val jsonArray = JSONArray(tabsJson)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val tab = Tab(
                        id = jsonObject.getString("id"),
                        title = jsonObject.getString("title"),
                        url = jsonObject.getString("url"),
                        favicon = jsonObject.optString("favicon").takeIf { it.isNotEmpty() },
                        isActive = jsonObject.getBoolean("isActive"),
                        createdAt = jsonObject.getLong("createdAt")
                    )
                    tabs.add(tab)
                }
                activeTabId = prefs.getString(KEY_ACTIVE_TAB, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // 如果没有标签页，创建一个默认的
        if (tabs.isEmpty()) {
            createNewTab()
        }
    }
    
    /**
     * 设置监听回调
     */
    interface TabChangeListener {
        fun onTabCreated(tab: Tab)
        fun onTabRemoved(tabId: String)
        fun onTabSwitched(tab: Tab)
        fun onTabUpdated(tab: Tab)
    }
}