package com.example.browser

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.browser.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    
    // 搜索引擎URL模板
    private val searchEngines = mapOf(
        "百度" to "https://www.baidu.com/s?wd=",
        "搜狗" to "https://www.sogou.com/web?query=",
        "必应" to "https://www.bing.com/search?q=",
        "抖音" to "https://www.douyin.com/search/"
    )
    
    private var currentSearchEngine = "百度"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSearchEngineSpinner()
        setupSearchInput()
        setupQuickAccessButtons()
    }

    private fun setupSearchEngineSpinner() {
        val engines = listOf("百度", "搜狗", "必应", "抖音")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, engines)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSearchEngine.adapter = adapter

        binding.spinnerSearchEngine.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSearchEngine = engines[position]
                updateEngineIcon()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateEngineIcon() {
        // 根据选择的搜索引擎更新图标
        val iconRes = when (currentSearchEngine) {
            "百度" -> R.drawable.ic_baidu
            "搜狗" -> R.drawable.ic_sogou
            "必应" -> R.drawable.ic_bing
            "抖音" -> R.drawable.ic_douyin
            else -> R.drawable.ic_search
        }
        binding.btnEngineIcon.setImageResource(iconRes)
    }

    private fun setupSearchInput() {
        // 搜索按钮点击
        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        // 搜索引擎图标点击 - 显示下拉菜单
        binding.btnEngineIcon.setOnClickListener {
            binding.spinnerSearchEngine.performClick()
        }

        // 键盘搜索按钮
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch()
                return@setOnEditorActionListener true
            }
            false
        }

        // 输入框点击聚焦
        binding.etSearch.setOnClickListener {
            binding.etSearch.isFocusableInTouchMode = true
            binding.etSearch.requestFocus()
        }
    }

    private fun performSearch() {
        val query = binding.etSearch.text.toString().trim()
        if (query.isEmpty()) {
            Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show()
            return
        }

        // 检查是否是网址
        val url = if (isUrl(query)) {
            if (query.startsWith("http://") || query.startsWith("https://")) {
                query
            } else {
                "https://$query"
            }
        } else {
            buildSearchUrl(query)
        }

        openBrowser(url)
    }

    private fun isUrl(query: String): Boolean {
        return query.contains(".") && !query.contains(" ")
    }

    private fun buildSearchUrl(query: String): String {
        val baseUrl = searchEngines[currentSearchEngine] ?: searchEngines["百度"]!!
        return baseUrl + java.net.URLEncoder.encode(query, "UTF-8")
    }

    private fun openBrowser(url: String) {
        val intent = Intent(this, MultiWindowBrowserActivity::class.java).apply {
            putExtra("url", url)
        }
        startActivity(intent)
    }

    private fun setupQuickAccessButtons() {
        // 百度快捷访问
        binding.btnBaidu.setOnClickListener {
            openQuickAccess("百度", "https://www.baidu.com")
        }

        // 搜狗快捷访问
        binding.btnSogou.setOnClickListener {
            openQuickAccess("搜狗", "https://www.sogou.com")
        }

        // 必应快捷访问
        binding.btnBing.setOnClickListener {
            openQuickAccess("必应", "https://www.bing.com")
        }

        // 抖音快捷访问
        binding.btnDouyin.setOnClickListener {
            openQuickAccess("抖音", "https://www.douyin.com")
        }
    }

    private fun openQuickAccess(name: String, url: String) {
        // 设置当前搜索引擎
        currentSearchEngine = name
        val position = listOf("百度", "搜狗", "必应", "抖音").indexOf(name)
        if (position >= 0) {
            binding.spinnerSearchEngine.setSelection(position)
        }
        
        openBrowser(url)
    }
}