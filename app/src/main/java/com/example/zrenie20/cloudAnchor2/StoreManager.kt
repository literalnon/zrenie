package com.example.zrenie20.cloudAnchor2

import android.content.Context
import android.util.Log
import com.example.zrenie20.cloudAnchor2.StorageManager.ShortCodeListener
import com.example.zrenie20.data.DataItemId
import com.example.zrenie20.data.RenderableCloudId
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

/** Helper class for Firebase storage of cloud anchor IDs.  */
class StorageManager(context: Context?) {

    /** Listener for a new Cloud Anchor ID from the Firebase Database.  */
    interface CloudAnchorIdListener {
        //fun onCloudAnchorIdAvailable(cloudAnchorId: String?, itemId: DataItemId?)
        fun onChildAdded(cloudAnchorId: String?, itemId: DataItemId?, renderableCloudId: RenderableCloudId?)
        fun onChildChanged(cloudAnchorId: String?, itemId: DataItemId?, renderableCloudId: RenderableCloudId?)
        fun onChildRemoved(cloudAnchorId: String?, itemId: DataItemId?, renderableCloudId: RenderableCloudId?)
    }

    /** Listener for a new short code from the Firebase Database.  */
    interface ShortCodeListener {
        fun onShortCodeAvailable(shortCode: Int?)
    }

    private val rootRef: DatabaseReference

    fun addSubscriber() {
        rootRef
            .child(KEY_SUBSCRIBER)
            .runTransaction(
                object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        var shortCode = currentData.getValue(Int::class.java)
                        if (shortCode == null) {
                            shortCode = 0
                        }
                        currentData.value = shortCode + 1
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?
                    ) {
                        if (committed) {
                            Log.e("MainActivityStorege", "addSubscriber")
                        }
                    }
                })
    }

    fun removeSubscriber() {
        rootRef
            .child(KEY_SUBSCRIBER)
            .runTransaction(
                object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val shortCode = currentData.getValue(Int::class.java) ?: 1

                        currentData.value = shortCode - 1

                        Log.e("MainActivityStorege", "removeSubscriber currentData.value : ${currentData.value}")

                        if (currentData.value.toString() == "0") {
                            Log.e("MainActivityStorege", "removeSubscriber currentData.value == 0")

                            rootRef.root.removeValue()
                        }

                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?
                    ) {
                        if (committed) {
                            Log.e("MainActivityStorege", "removeSubscriber")
                        }
                    }
                })
    }

    /** Gets a new short code that can be used to store the anchor ID.  */
    fun nextShortCode(listener: ShortCodeListener) {
        // Run a transaction on the node containing the next short code available. This increments the
        // value in the database and retrieves it in one atomic all-or-nothing operation.
        rootRef
            .child(KEY_NEXT_SHORT_CODE)
            .runTransaction(
                object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        var shortCode = currentData.getValue(Int::class.java)
                        if (shortCode == null) {
                            shortCode = INITIAL_SHORT_CODE - 1
                        }
                        currentData.value = shortCode + 1
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?
                    ) {
                        if (!committed) {
                            Log.e(TAG, "Firebase Error", error!!.toException())
                            listener.onShortCodeAvailable(null)
                        } else {
                            listener.onShortCodeAvailable(currentData!!.getValue(Int::class.java))
                        }
                    }
                })
    }

    /** Stores the cloud anchor ID in the configured Firebase Database.  */
    fun storeUsingShortCodeWithId(shortCode: Int, cloudAnchorId: String, itemId: String) {
        rootRef.child(KEY_PREFIX + shortCode + DELIMITER + itemId)
            .setValue(cloudAnchorId)
    }

    /** Stores the cloud anchor ID in the configured Firebase Database.  */
    /*fun storeUsingShortCode(shortCode: Int, cloudAnchorId: String?) {
        rootRef.child(KEY_PREFIX + shortCode).setValue(cloudAnchorId)
    }*/

    fun getCloudAnchor(listener: CloudAnchorIdListener) {
        rootRef
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.e(
                        TAG,
                        "onChildAdded: " + snapshot.value + " a " + snapshot.key + " : " + snapshot.exists()
                    )

                    val ids = snapshot.key
                        .toString()
                        .split(DELIMITER)

                    val cloudId = snapshot.value?.toString()

                    val renderableCloudId = ids.firstOrNull()
                        ?.replace(KEY_PREFIX, "")
                        ?.toLongOrNull()

                    val itemId = ids.lastOrNull()
                        ?.toLongOrNull()

                    /*Log.e(
                        TAG,
                        "onChildAdded cloudId : ${cloudId}, itemId : ${itemId}, renderableCloudId : ${renderableCloudId}"
                    )*/
                    if (cloudId != null && itemId != null) {
                        listener.onChildAdded(cloudId, itemId, renderableCloudId)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.e(
                        TAG,
                        "onChildChanged: " + snapshot.value + " a " + snapshot.key + " : " + snapshot.exists()
                    )

                    val ids = snapshot.key
                        .toString()
                        .split(DELIMITER)

                    val cloudId = snapshot.value?.toString()

                    val renderableCloudId = ids.firstOrNull()
                        ?.replace(KEY_PREFIX, "")
                        ?.toLongOrNull()

                    val itemId = ids.lastOrNull()
                        ?.toLongOrNull()

                    Log.e(
                        TAG,
                        "onChildChanged cloudId : ${cloudId}, itemId : ${itemId}, renderableCloudId : ${renderableCloudId}"
                    )

                    if (cloudId != null && itemId != null && renderableCloudId != null) {
                        listener.onChildChanged(cloudId, itemId, renderableCloudId)
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    Log.e(
                        TAG,
                        "onChildRemoved: " + snapshot.value + " a " + snapshot.key + " : " + snapshot.exists()
                    )

                    val ids = snapshot.key
                        .toString()
                        .split(DELIMITER)

                    val cloudId = snapshot.value?.toString()

                    val renderableCloudId = ids.firstOrNull()
                        ?.replace(KEY_PREFIX, "")
                        ?.toLongOrNull()

                    val itemId = ids.lastOrNull()
                        ?.toLongOrNull()

                    Log.e(
                        TAG,
                        "onChildRemoved cloudId : ${cloudId}, itemId : ${itemId}, renderableCloudId : ${renderableCloudId}"
                    )

                    if (cloudId != null && itemId != null&& renderableCloudId != null) {
                        listener.onChildRemoved(cloudId, itemId, renderableCloudId)
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.e(
                        TAG,
                        "onChildMoved:"
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        TAG,
                        "onCancelled:"
                    )
                }

            })
    }

    fun removeChild(renderableCloudId: RenderableCloudId?, itemId: DataItemId) {
        Log.e(
            TAG,
            "renderableRemove 2removeChild ${KEY_PREFIX + renderableCloudId + DELIMITER + itemId}"
        )

        rootRef?.child(KEY_PREFIX + renderableCloudId + DELIMITER + itemId)
            .removeValue()
    }
    /**
     * Retrieves the cloud anchor ID using a short code. Returns an empty string if a cloud anchor ID
     * was not stored for this short code.
     */
    /*fun getCloudAnchorID(shortCode: Int, listener: CloudAnchorIdListener) {
        rootRef
            .child(KEY_PREFIX + shortCode)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        Log.e(
                            TAG,
                            "onDataChange: " + dataSnapshot.value + " a " + dataSnapshot.key + " : " + dataSnapshot.exists()
                        )
                        val ids = dataSnapshot.value.toString().split(DELIMITER)

                        listener.onCloudAnchorIdAvailable(ids.firstOrNull(), ids.getOrNull(1)?.toLongOrNull())

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(
                            TAG, "The database operation for getCloudAnchorID was cancelled.",
                            error.toException()
                        )
                        listener.onCloudAnchorIdAvailable(null, null)
                    }
                })
    }*/

    companion object {
        private const val TAG = "MainActivityCloudAnchors" //StorageManager.class.getName();
        private const val KEY_ROOT_DIR = "shared_anchor_codelab_root"
        private const val KEY_NEXT_SHORT_CODE = "next_short_code"
        private const val KEY_PREFIX = "anchor;"
        private const val INITIAL_SHORT_CODE = 142
        private const val KEY_SUBSCRIBER = "subscriber"

        const val DELIMITER = ":"
    }

    init {
        val firebaseApp = FirebaseApp.initializeApp(context!!)
        rootRef = FirebaseDatabase.getInstance(firebaseApp!!).reference.child(KEY_ROOT_DIR)
        DatabaseReference.goOnline()
    }
}