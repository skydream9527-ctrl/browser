package com.example.browser

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.browser.data.Tab
import com.example.browser.data.TabManager
import com.example.browser.databinding.ActivityMultiWindowBrowserBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.net.URL

class MultiWindowBrowserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMultiWindowBrowserBinding
    private lateinit var tabManager: TabManager
    private val webViews = mutableMapOf<String, WebView>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiWindowBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        tabManager = TabManager(this)
        
        initToolbar()
        initWebViewContainer()
        initBottomBar()
        
        // 处理传入的URL
        val url = intent.getStringExtra("url")
        if (url != null) {
            // 如果有传入URL，在新标签页打开
            val tab = tabManager.createNewTab(url)
            loadUrlInTab(tab, url)
        } else {
            // 切换到当前活动标签页
            val activeTab = tabManager.getActiveTab()
            if (activeTab != null && activeTab.url.isNotEmpty()) {
                showTab(activeTab)
            }
        }
    }
    
    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        // 地址栏
        binding.etUrl.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO) {
                loadUrl(binding.etUrl.text.toString())
                true
            } else {
                false
            }
        }
        
        // 刷新按钮
        binding.btnRefresh.setOnClickListener {
            val activeTab = tabManager.getActiveTab()
            activeTab?.let { tab ->
                webViews[tab.id]?.reload()
            }
        }
    }
    
    private fun initWebViewContainer() {
        // WebView容器已准备在XML中
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(tab: Tab): WebView {
        val webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            
            val settings = this.settings
            settings.javaScriptEnabled = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
                }
                
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.progress = 0
                    url?.let {
                        binding.etUrl.setText(it)
                        tabManager.updateTab(tab.id, url = it)
                    }
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.visibility = View.GONE
                    view?.title?.let {
                        tabManager.updateTab(tab.id, title = it)
                    }
                }
            }
            
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    binding.progressBar.progress = newProgress
                }
                
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    title?.let {
                        tabManager.updateTab(tab.id, title = it)
                    }
                }
            }
        }
        
        webViews[tab.id] = webView
        return webView
    }
    
    private fun loadUrlInTab(tab: Tab, url: String) {
        var webView = webViews[tab.id]
        if (webView == null) {
            webView = createWebView(tab)
        }
        
        val finalUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "https://$url"
        }
        
        webView.loadUrl(finalUrl)
        tabManager.updateTab(tab.id, url = finalUrl)
    }
    
    private fun showTab(tab: Tab) {
        // 隐藏所有WebView
        webViews.values.forEach { it.visibility = View.GONE }
        
        // 显示指定标签页的WebView
        var webView = webViews[tab.id]
        if (webView == null) {
            webView = createWebView(tab)
            binding.webViewContainer.addView(webView)
        }
        webView.visibility = View.VISIBLE
        
        // 更新地址栏
        if (tab.url.isNotEmpty()) {
            binding.etUrl.setText(tab.url)
        }
        
        // 更新标签计数
        updateTabCount()
    }
    
    private fun loadUrl(url: String) {
        val activeTab = tabManager.getActiveTab()
        if (activeTab != null) {
            loadUrlInTab(activeTab, url)
        } else {
            val newTab = tabManager.createNewTab()
            loadUrlInTab(newTab, url)
        }
    }
    
    private fun initBottomBar() {
        // 返回按钮
        binding.btnBack.setOnClickListener {
            val activeTab = tabManager.getActiveTab()
            activeTab?.let { tab ->
                webViews[tab.id]?.goBack()
            }
        }
        
        // 前进按钮
        binding.btnForward.setOnClickListener {
            val activeTab = tabManager.getActiveTab()
            activeTab?.let { tab ->
                webViews[tab.id]?.goForward()
            }
        }
        
        // 主页按钮
        binding.btnHome.setOnClickListener {
            finish()
        }
        
        // 多窗口/标签页按钮
        binding.btnTabs.setOnClickListener {
            showTabsDialog()
        }
        
        // 菜单按钮
        binding.btnMenu.setOnClickListener {
            showMenuDialog()
        }
        
        updateTabCount()
    }
    
    private fun showTabsDialog() {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_tabs, null)
        
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerTabs)
        val btnNewTab = view.findViewById<View>(R.id.btnNewTab)
        val btnCloseAll = view.findViewById<View>(R.id.btnCloseAll)
        
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = TabsAdapter(tabManager.getAllTabs()) { action, tab ->
            when (action) {
                TabAction.SWITCH -> {
                    tabManager.switchToTab(tab.id)
                    showTab(tab)
                    bottomSheet.dismiss()
                }
                TabAction.CLOSE -> {
                    removeTab(tab.id)
                    recyclerView.adapter?.notifyDataSetChanged()
                    if (tabManager.getTabCount() == 0) {
                        bottomSheet.dismiss()
                    }
                }
            }
        }
        
        btnNewTab.setOnClickListener {
            val newTab = tabManager.createNewTab()
            showTab(newTab)
            bottomSheet.dismiss()
        }
        
        btnCloseAll.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("关闭所有标签页")
                .setMessage("确定要关闭所有标签页吗？")
                .setPositiveButton("确定") { _, _ ->
                    closeAllTabs()
                    bottomSheet.dismiss()
                }
                .setNegativeButton("取消", null)
                .show()
        }
        
        bottomSheet.setContentView(view)
        bottomSheet.show()
    }
    
    private fun showMenuDialog() {
        val items = arrayOf("分享", "刷新", "添加到收藏", "设置")
        AlertDialog.Builder(this)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> shareCurrentPage()
                    1 -> {
                        val activeTab = tabManager.getActiveTab()
                        activeTab?.let { tab ->
                            webViews[tab.id]?.reload()
                        }
                    }
                    2 -> addToFavorites()
                    3 -> openSettings()
                }
            }
            .show()
    }
    
    private fun shareCurrentPage() {
        val activeTab = tabManager.getActiveTab()
        activeTab?.let { tab ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, tab.url)
                putExtra(Intent.EXTRA_TITLE, tab.title)
            }
            startActivity(Intent.createChooser(intent, "分享"))
        }
    }
    
    private fun addToFavorites() {
        val activeTab = tabManager.getActiveTab()
        activeTab?.let { tab ->
            Toast.makeText(this, "已添加到收藏: ${tab.title}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openSettings() {
        Toast.makeText(this, "设置功能开发中", Toast.LENGTH_SHORT).show()
    }
    
    private fun removeTab(tabId: String) {
        // 从容器中移除WebView
        webViews[tabId]?.let { webView ->
            webView.stopLoading()
            webView.destroy()
            binding.webViewContainer.removeView(webView)
        }
        webViews.remove(tabId)
        
        // 从管理器移除
        tabManager.removeTab(tabId)
        
        // 显示新的活动标签页
        val activeTab = tabManager.getActiveTab()
        if (activeTab != null) {
            showTab(activeTab)
        } else {
            // 如果没有标签页了，返回主页
            finish()
        }
        
        updateTabCount()
    }
    
    private fun closeAllTabs() {
        // 清理所有WebView
        webViews.values.forEach { webView ->
            webView.stopLoading()
            webView.destroy()
        }
        webViews.clear()
        binding.webViewContainer.removeAllViews()
        
        // 清空管理器
        tabManager.closeAllTabs()
        
        // 创建一个新标签页
        val newTab = tabManager.createNewTab()
        showTab(newTab)
        
        updateTabCount()
    }
    
    private fun updateTabCount() {
        val count = tabManager.getTabCount()
        // ImageButton 不支持 text 属性，使用 contentDescription 或忽略
        binding.btnTabs.contentDescription = "标签页 ($count)"
    }
    
    override fun onBackPressed() {
        val activeTab = tabManager.getActiveTab()
        val webView = activeTab?.let { webViews[it.id] }
        
        if (webView?.canGoBack() == true) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 清理所有WebView
        webViews.values.forEach { webView ->
            webView.stopLoading()
            webView.destroy()
        }
        webViews.clear()
    }
    
    enum class TabAction {
        SWITCH, CLOSE
    }
    
    inner class TabsAdapter(
        private var tabs: List<Tab>,
        private val onAction: (TabAction, Tab) -> Unit
    ) : RecyclerView.Adapter<TabsAdapter.TabViewHolder>() {
        
        inner class TabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvTitle: TextView = itemView.findViewById(R.id.tvTabTitle)
            val tvUrl: TextView = itemView.findViewById(R.id.tvTabUrl)
            val btnClose: ImageButton = itemView.findViewById(R.id.btnCloseTab)
            val cardView: View = itemView.findViewById(R.id.cardTab)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tab, parent, false)
            return TabViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
            val tab = tabs[position]
            holder.tvTitle.text = tab.title
            holder.tvUrl.text = try {
                URL(tab.url).host
            } catch (e: Exception) {
                tab.url
            }
            
            // 活动标签页高亮
            holder.cardView.alpha = if (tab.isActive) 1.0f else 0.7f
            
            holder.cardView.setOnClickListener {
                onAction(TabAction.SWITCH, tab)
            }
            
            holder.btnClose.setOnClickListener {
                onAction(TabAction.CLOSE, tab)
            }
        }
        
        override fun getItemCount(): Int = tabs.size
        
        fun updateTabs(newTabs: List<Tab>) {
            tabs = newTabs
            notifyDataSetChanged()
        }
    }
}