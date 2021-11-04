package com.example.zrenie20.binakular

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.oussaki.rxfilesdownloader.*
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import java.net.URL


interface IFileDownloadManager {
    fun downloadFile(url: String, context: Context): Single<File>
}

class FileDownloadManager : IFileDownloadManager {

    fun getAllFiles(context: Context): ArrayList<String> {
        val dirPath = context.cacheDir.path

        val filePath = StringBuilder()
            .append(dirPath)
            .append(File.separator)
            .toString()

        val filesPaths = arrayListOf<String>()

        filesPaths.addAll(
            File(filePath).listFiles().map {
                it.absolutePath
            }
        )

        return filesPaths
    }

    override fun downloadFile(url: String, context: Context): Single<File> {
        val paths = url.split(File.separator)
        val absolutePath = StringBuilder()
            .append(paths.getOrNull(paths.size - 1) ?: "defaultFilename")
            .toString()

        val dirPath = context.cacheDir.path

        val filePath = StringBuilder()
            .append(dirPath)
            .append(File.separator)
            .append(absolutePath)
            .toString()

        val newFile = File(filePath)

        Log.e("FileDownloadManager", "newFile : ${newFile.exists()}")
        Log.e("FileDownloadManager", "newFile : ${newFile.absolutePath}")

        return if (newFile.exists()) {
            Log.e("FileDownloadManager", "exist")

            Single.just(newFile)
        } else {
            RxDownloader.Builder(context)
                //.addFile("https://github.com/literalnon/AR/raw/master/app/src/main/models/3.glb")
                .addFile(
                    absolutePath,
                    url
                )
                .build()
                .asList()
                .map {
                    val curFile = it.first()
                    Log.e("FileDownloadManager", "curFile : ${curFile.file.absolutePath}")

                    //val newFile = File(curFile.file.absolutePath)
                    newFile.createNewFile()

                    val fos = newFile.outputStream()//FileOutputStream(filepath)
                    //val buffer = "This will be writtent in test.txt".toByteArray()
                    fos.write(curFile.bytes, 0, curFile.bytes.size)
                    fos.close()

                    Log.e("FileDownloadManager", "curFile : ${curFile.file.absolutePath}")

                    newFile
                }
        }
    }

    fun removeAllFiles(context: Context) {
        val dirPath = context.cacheDir.path

        val file = File(dirPath)
        file.listFiles().forEach {
            it.delete()
        }
    }

    fun removeFile(filePath: String) {
        val file = File(filePath)
        file.delete()
    }
}
/*
class FileDownloadManager: IFileDownloadManager {
    init {
        val mOnFileDownloadStatusListener: OnFileDownloadStatusListener =
            object : OnSimpleFileDownloadStatusListener() {
                override fun onFileDownloadStatusRetrying(
                    downloadFileInfo: DownloadFileInfo,
                    retryTimes: Int
                ) {
                    Log.e("FileDownloadManager", "onFileDownloadStatusRetrying retrying download when failed once, the retryTimes is the current trying times")
                }

                override fun onFileDownloadStatusWaiting(downloadFileInfo: DownloadFileInfo) {
                    Log.e("FileDownloadManager", "onFileDownloadStatusWaiting waiting for download(wait for other tasks paused, or FileDownloader is busy for other operations)")
                }

                override fun onFileDownloadStatusPreparing(downloadFileInfo: DownloadFileInfo) {
                    Log.e("FileDownloadManager", "onFileDownloadStatusPreparing preparing(connecting)")
                }

                override fun onFileDownloadStatusPrepared(downloadFileInfo: DownloadFileInfo) {
                    Log.e("FileDownloadManager", "onFileDownloadStatusPrepared prepared(connected)")
                }

                override fun onFileDownloadStatusDownloading(
                    downloadFileInfo: DownloadFileInfo,
                    downloadSpeed: Float,
                    remainingTime: Long
                ) {
                     Log.e("FileDownloadManager", "onFileDownloadStatusDownloading downloading, the downloadSpeed with KB/s unit, the remainingTime with seconds unit")
                }

                override fun onFileDownloadStatusPaused(downloadFileInfo: DownloadFileInfo) {
                     Log.e("FileDownloadManager", "onFileDownloadStatusPaused download paused")
                }

                override fun onFileDownloadStatusCompleted(downloadFileInfo: DownloadFileInfo) {
                     Log.e("FileDownloadManager", "onFileDownloadStatusCompleted download completed(the url file has been finished)")
                }

                override fun onFileDownloadStatusFailed(
                    url: String,
                    downloadFileInfo: DownloadFileInfo,
                    failReason: FileDownloadStatusFailReason
                ) {
                     Log.e("FileDownloadManager", "onFileDownloadStatusFailed error occur, see failReason for details, some of the failReason you must concern")
                    val failType = failReason.type
                    val failUrl =
                        failReason.url // or failUrl = url, both url and failReason.getUrl() are the same
                    if (FileDownloadStatusFailReason.TYPE_URL_ILLEGAL == failType) {
                        // the url error when downloading file with failUrl
                    } else if (FileDownloadStatusFailReason.TYPE_STORAGE_SPACE_IS_FULL == failType) {
                        // storage space is full when downloading file with failUrl
                    } else if (FileDownloadStatusFailReason.TYPE_NETWORK_DENIED == failType) {
                        // network access denied when downloading file with failUrl
                    } else if (FileDownloadStatusFailReason.TYPE_NETWORK_TIMEOUT == failType) {
                        // connect timeout when downloading file with failUrl
                    } else {
                        // more....
                    }

                    // exception details
                    val failCause = failReason.cause // or failReason.getOriginalCause()

                    // also you can see the exception message
                    val failMsg =
                        failReason.message // or failReason.getOriginalCause().getMessage()

                    Log.e("FileDownloadManager", "onFileDownloadStatusFailed failCause : ${failCause}")
                    Log.e("FileDownloadManager", "onFileDownloadStatusFailed failMsg : ${failMsg}")
                    Log.e("FileDownloadManager", "onFileDownloadStatusFailed failUrl : ${failUrl}")
                }
            }
        FileDownloader.registerDownloadStatusListener(mOnFileDownloadStatusListener)
    }

    override fun downloadFile(url: String) {
        Log.e("FileDownloadManager", "downloadFile FileDownloader.getDownloadDir() : ${FileDownloader.getDownloadDir()}")
        FileDownloader.start(url)
    }
}*/

