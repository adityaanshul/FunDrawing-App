package com.adityapp.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream



class MainActivity : AppCompatActivity()  {
    private var kt_drawingView : ClassDrawingView? = null //object of a Class -> ClassDrawingView

    private var kt_imageBtnCurrentPaint : ImageButton? = null

    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if(result.resultCode == RESULT_OK && result.data!=null){
                val imageBackGround : ImageView = findViewById(R.id.iv_background)
                imageBackGround.setImageURI(result.data?.data)
            }
        }

    private val requestPermissionLauncher : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permissions ->
            permissions.entries.forEach{
                val permissionName = it.key
                val isGranted = it.value

                if(isGranted){

                    val pickIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)


                } else {
                    if(permissionName=== Manifest.permission.READ_EXTERNAL_STORAGE){
                        Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        kt_drawingView = findViewById(R.id.drawing_view)
        kt_drawingView?.brushSizeSetter(10.toFloat())

        val linearLayoutPaintColors: LinearLayout = findViewById(R.id.layout_colors)
        kt_imageBtnCurrentPaint = linearLayoutPaintColors[0] as ImageButton

        kt_imageBtnCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.color_pallet_pressed)
        )

        val kt_btn_brush : ImageButton = findViewById(R.id.ib_brush)
        kt_btn_brush.setOnClickListener {
            showBrushSizePickerDialog()
        }

        val kt_btn_undo : Button = findViewById(R.id.btn_undo)
            kt_btn_undo.setOnClickListener{
                kt_drawingView?.onClickUndo()
            }
        val kt_btn_save : Button = findViewById(R.id.btn_save)
        kt_btn_save.setOnClickListener{

            if(isReadStorageAllowed()){
                lifecycleScope.launch{
                    val fl_drawingView: FrameLayout = findViewById(R.id.fl_drawing_container)
                    saveBitmapFile(getBitmapFromView((fl_drawingView)))
                }
            }

        }

        val kt_btn_gallery : Button = findViewById(R.id.btn_gallery)
        kt_btn_gallery.setOnClickListener {
            requestStoragePermission()
        }
    }

    private fun showBrushSizePickerDialog(){
        val brushDialog = Dialog(this)       //dialog() object is created
        brushDialog.setContentView(R.layout.brush_size_dialoge)
        val smallBtn : ImageButton = brushDialog.findViewById(R.id.small_brush)
        val mediumBtn : ImageButton = brushDialog.findViewById(R.id.medium_brush)
        val largeBtn : ImageButton = brushDialog.findViewById(R.id.large_brush)
        smallBtn.setOnClickListener{
            kt_drawingView?.brushSizeSetter(10.toFloat())
            brushDialog.dismiss()
        }

        mediumBtn.setOnClickListener{
            kt_drawingView?.brushSizeSetter(20.toFloat())
            brushDialog.dismiss()
        }

        largeBtn.setOnClickListener{
            kt_drawingView?.brushSizeSetter(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()

    }

    private fun isReadStorageAllowed() : Boolean{
        val result = ContextCompat.checkSelfPermission(this,
        Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            showRationalDialog("Kids Drawing App", "Kids Drawing App" + "need to Access Your External Storage")
        } else {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
    }

    private fun showRationalDialog(title: String, message: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(message).setPositiveButton("Cancel"){dialog, _-> }
        builder.create().show()
    }

    private fun getBitmapFromView(view: View) : Bitmap{
        val returnedBitmap = Bitmap.createBitmap(view.width,
            view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if(bgDrawable != null){
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return returnedBitmap

    }

    private suspend fun saveBitmapFile(mBitmap: Bitmap?): String{
        var result = ""
        withContext(Dispatchers.IO){
            if(mBitmap != null){
                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    val f = File(externalCacheDir?.absoluteFile.toString()
                            + File.separator + "KidDrawingApp" +
                            System.currentTimeMillis()/1000 + ".png")

                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()

                    result = f.absolutePath

                    runOnUiThread{
                        if(result.isNotEmpty()){
                            Toast.makeText(this@MainActivity, "File saved successfully: $result",
                                Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Something Went Wrong Like Your Birth",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                catch (e : Exception){
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    fun paintClicked(view: View){
        if(view !== kt_imageBtnCurrentPaint) {
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            kt_drawingView?.setColor(colorTag)

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.color_pallet_pressed)
            )

            kt_imageBtnCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.color_pallet)
            )

            kt_imageBtnCurrentPaint = view
        }
    }
}
