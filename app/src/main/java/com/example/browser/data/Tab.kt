package com.example.browser.data

import java.util.UUID

/**
 * 浏览器标签页/窗口数据类
 */
data class Tab(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "新标签页",
    var url: String = "",
    var favicon: String? = null,
    var isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TAB_ID_NEW = "new_tab"
    }
}