package com.example.zrenie20

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zrenie20.base.adapters.DelegationAdapter
import com.example.zrenie20.data.*
import com.example.zrenie20.myarsample.BaseArActivity
import com.example.zrenie20.space.FileDownloadManager
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_lib.*
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.activity_search.ivBack
import kotlinx.android.synthetic.main.activity_search.rvAr

class SearchActivity : AppCompatActivity() {
    val adapter = DelegationAdapter<Any>()
    open var assetsArray = arrayListOf<Any>()
    val fileDownloadManager = FileDownloadManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val allFiles = fileDownloadManager.getAllFiles(this)

        ivBack?.setOnClickListener {
            onBackPressed()
        }

        rvAr.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        rvAr.layoutManager = layoutManager

        adapter?.manager?.addDelegate(
            LibAdapter(onSelectedItem = {

            })
        )

        adapter?.manager?.addDelegate(
            LibDataItemObjectAdapter(onSelectedItem = {
                if (it.trigger?.type?.codeName == ArTypes.ArGeoType().codeName) {
                    val gmmIntentUri: Uri = Uri.parse("google.navigation:q=${it.trigger.latitude},${it.trigger.longitude}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }
            })
        )

        Realm.getDefaultInstance()
            .executeTransaction { realm ->
                val objects = realm.where(RealmDataItemObject::class.java)
                    .findAll()
                    .map { it.toDataItemObject() }
                    .filter { dataItemObj ->
                        allFiles?.filter {
                            it.contains(dataItemObj.filePath?.split("/")?.lastOrNull() ?: " ")
                        }?.isNotEmpty() == true
                    }

                assetsArray.clear()

                assetsArray.addAll(objects)

                adapter.replaceAll(assetsArray)
            }
    }
}