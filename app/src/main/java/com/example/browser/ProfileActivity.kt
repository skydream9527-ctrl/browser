package com.example.browser

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.browser.databinding.ActivityProfileBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var currentPhotoPath: String? = null
    private var avatarFile: File? = null

    companion object {
        private const val REQUEST_CODE_CAMERA = 1001
        private const val REQUEST_CODE_GALLERY = 1002
        private const val REQUEST_CODE_PERMISSION = 1003
        private const val AVATAR_FILENAME = "user_avatar.jpg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadAvatar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadAvatar() {
        avatarFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), AVATAR_FILENAME)
        if (avatarFile?.exists() == true) {
            val bitmap = BitmapFactory.decodeFile(avatarFile!!.absolutePath)
            binding.ivAvatar.setImageBitmap(bitmap)
        }
    }

    private fun setupClickListeners() {
        // 点击头像区域更换头像
        binding.cardAvatar.setOnClickListener {
            showImageSourceDialog()
        }
        binding.tvEditAvatar.setOnClickListener {
            showImageSourceDialog()
        }

        // 菜单项点击
        binding.menuFavorites.setOnClickListener {
            Toast.makeText(this, "收藏功能开发中", Toast.LENGTH_SHORT).show()
        }
        binding.menuHistory.setOnClickListener {
            Toast.makeText(this, "历史记录功能开发中", Toast.LENGTH_SHORT).show()
        }
        binding.menuSettings.setOnClickListener {
            Toast.makeText(this, "设置功能开发中", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("拍照", "从相册选择")
        AlertDialog.Builder(this)
            .setTitle("选择头像来源")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkPermissionAndOpenCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun checkPermissionAndOpenCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_PERMISSION
                )
            } else {
                openCamera()
            }
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (e: IOException) {
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(intent, REQUEST_CODE_CAMERA)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "avatar_${System.currentTimeMillis()}",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CAMERA -> {
                    currentPhotoPath?.let { path ->
                        val bitmap = BitmapFactory.decodeFile(path)
                        bitmap?.let {
                            val croppedBitmap = cropToSquare(it)
                            saveAvatar(croppedBitmap)
                            binding.ivAvatar.setImageBitmap(croppedBitmap)
                        }
                    }
                }
                REQUEST_CODE_GALLERY -> {
                    data?.data?.let { uri ->
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        val croppedBitmap = cropToSquare(bitmap)
                        saveAvatar(croppedBitmap)
                        binding.ivAvatar.setImageBitmap(croppedBitmap)
                    }
                }
            }
        }
    }

    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    private fun saveAvatar(bitmap: Bitmap) {
        try {
            val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), AVATAR_FILENAME)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            Toast.makeText(this, "头像已保存", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, "保存头像失败", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}