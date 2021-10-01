package com.example.zrenie20.augmentedFace.augmentedfaces

import android.util.Log
import com.google.ar.core.AugmentedFace
import com.google.ar.core.AugmentedFace.RegionType
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.SkeletonNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.rendering.RenderableDefinition.Submesh
import com.google.ar.sceneform.rendering.Vertex.UvCoordinate
import com.google.ar.sceneform.ux.AugmentedFaceNode
import java.util.*
import com.example.zrenie20.R
import com.example.zrenie20.data.TypeItemObjectCodeNames
import com.example.zrenie20.renderable.IArRenderObject
import java.util.concurrent.ExecutionException
import java.util.function.BiFunction

class MyAugmentedFaceNode: Node {
    /** Returns the AugmentedFace that this Node is applying visual effects to.  */
    /** Sets the AugmentedFace that this node is applying visual effects to.  */
    // The augmented face to render visual effects for.
    var augmentedFace: AugmentedFace? = null
    var iArRenderObject: IArRenderObject? = null

    // Fields for nodes.
    private val faceMeshNode: Node
    private val faceRegionsSkeletonNode: SkeletonNode

    // Fields for face mesh renderable.
    private val vertices = ArrayList<Vertex>()
    private val triangleIndices = ArrayList<Int>()
    private val submeshes = ArrayList<Submesh>()
    private val faceMeshDefinition: RenderableDefinition

    private var faceMeshRenderable: ModelRenderable? = null

    private var defaultFaceMeshMaterial: Material? = null

    private var overrideFaceMeshMaterial: Material? = null

    private var faceMeshOccluderMaterial: Material? = null

    private var faceMeshTexture: Texture? = null

    /** Create an AugmentedFaceNode with the given AugmentedFace.  */
    constructor(augmentedFace: AugmentedFace?) : super() {
        this.augmentedFace = augmentedFace
    }

    /**
     * Returns the texture rendered on the face mesh. Defaults to null.
     *
     *
     * Note: This is only used if the face mesh material hasn't been overridden.
     */
    fun getFaceMeshTexture(): Texture? {
        return faceMeshTexture
    }

    /**
     * Sets the texture rendered on the face mesh.
     *
     *
     * Note: This is only used if the face mesh material hasn't been overridden.
     */
    fun setFaceMeshTexture (texture: Texture?) {
        faceMeshTexture = texture
        updateSubmeshes()
    }
    /**
     * Returns the Renderable that is mapped to the regions o the face. It must be rigged with bones
     * that match the face regions. Use the provided sample .fbx file to export a face regions
     * renderable in the correct format.
     */
    /**
     * Sets the Renderable that is mapped to the regions of the face. It must be rigged with bones
     * that match the face regions. Use the provided sample .fbx file to export a face regions
     * renderable in the correct format.
     */
    var faceRegionsRenderable: ModelRenderable?
        get() {
            val renderable = faceRegionsSkeletonNode.renderable
            check(!(renderable != null && renderable !is ModelRenderable)) { "Face Regions Renderable must be a ModelRenderable." }
            return renderable as ModelRenderable?
        }
        set(renderable) {
            faceRegionsSkeletonNode.renderable = renderable
        }
    /** Returns the material currently overriding how the face mesh is rendered. Defaults to null.  */
    /**
     * Sets the material used to render the face mesh. The overriding material will not use [ ][.getFaceMeshTexture]. Set back to null to revert to the default material.
     */
    var faceMeshMaterialOverride: Material?
        get() = overrideFaceMeshMaterial
        set(material) {
            overrideFaceMeshMaterial = material
            updateSubmeshes()
        }

    override fun onActivate() {
        val scene: Scene = checkNotNull<Scene>(
            scene
        )
        val context = scene.view.context
        // Face mesh material is embedded in a dummy renderable.
        ModelRenderable.builder()
            .setSource(context, R.raw.sceneform_face_mesh)
            .build()
            .handle<Boolean>(
                BiFunction { renderable: ModelRenderable, throwable: Throwable? ->
                    if (throwable != null) {
                        Log.e(
                            TAG,
                            "Unable to load face mesh material.",
                            throwable
                        )
                        return@BiFunction false
                    }
                    defaultFaceMeshMaterial = renderable.material
                    updateSubmeshes()
                    true
                })

        // Face mesh occluder material is embedded in a dummy renderable.
        ModelRenderable.builder()
            .setSource(context, R.raw.sceneform_face_mesh_occluder)
            .build()
            .handle<Boolean>(
                BiFunction { renderable: ModelRenderable, throwable: Throwable? ->
                    if (throwable != null) {
                        Log.e(
                            TAG,
                            "Unable to load face mesh occluder material.",
                            throwable
                        )
                        return@BiFunction false
                    }
                    faceMeshOccluderMaterial = renderable.material
                    updateSubmeshes()
                    true
                })
    }

    override fun onUpdate(frameTime: FrameTime) {
        val isTracking = isTracking

        // Only render the visual effects when the augmented face is tracking.
        faceMeshNode.isEnabled = isTracking
        faceRegionsSkeletonNode.isEnabled = isTracking
        if (isTracking) {
            updateTransform()
            updateRegionNodes()
            updateFaceMesh()
        }
    }

    private val isTracking: Boolean
        private get() = augmentedFace != null && augmentedFace!!.trackingState == TrackingState.TRACKING

    private fun updateTransform() {
        // Update this node to be positioned at the center pose of the face.
        val pose: Pose =
            checkNotNull<AugmentedFace>(augmentedFace).getRegionPose(AugmentedFace.RegionType.NOSE_TIP)//.getCenterPose()
        worldPosition = Vector3(pose.tx(), pose.ty(), pose.tz())
        /*worldRotation =
            Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw())*/
        var rotation: Quaternion? = Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw())
        val inverse = Quaternion(Vector3(0.0f, 1.0f, 0.0f), 180.0f)
        rotation = Quaternion.multiply(rotation, inverse)

        if (iArRenderObject?.dataItemObject?.type?.codeName == TypeItemObjectCodeNames.VIDEO.codeName) {
            rotation = Quaternion()
            rotation.x = 1f

            /*rotation = Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw())
            val mInverse = Quaternion(Vector3(1.0f, 0.0f, 0.0f), 180.0f)
            rotation = Quaternion.multiply(rotation, mInverse)*/

            worldScale = Vector3(
                0.2f,
                0.2f,
                0.2f
            )
        }

        worldRotation = rotation
    }

    private fun updateRegionNodes() {
        // Update the pose of all the region nodes so that the bones in the face regions renderable
        // are driven by the regions of the augmented face.
        for (regionType in RegionType.values()) {
            val regionNode: Node = checkNotNull<Node>(
                faceRegionsSkeletonNode.getBoneAttachment(
                    boneNameForRegion(
                        regionType
                    )
                )
            )
            /*val pose: Pose = checkNotNull<AugmentedFace>(augmentedFace)
                .getRegionPose(regionType)*/
            val pose: Pose =
                checkNotNull<AugmentedFace>(augmentedFace).getRegionPose(AugmentedFace.RegionType.NOSE_TIP)
            regionNode.worldPosition = Vector3(pose.tx(), pose.ty(), pose.tz())

            // Rotate the bones by 180 degrees because the .fbx template's coordinate system is
            // inversed of Sceneform's coordinate system. This is so the .fbx works with other
            // 3D rendering engines as well.
            var rotation: Quaternion? = Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw())
            //val inverse = Quaternion(Vector3(0.0f, 1.0f, 0.0f), 180.0f)
            //rotation = Quaternion.multiply(rotation, inverse)
            regionNode.worldRotation = rotation
        }
    }

    private fun updateFaceMesh() {
        // Wait until the material is loaded.
        if (defaultFaceMeshMaterial == null || faceMeshOccluderMaterial == null) {
            return
        }
        updateFaceMeshVerticesAndTriangles()
        if (faceMeshRenderable == null) {
            try {
                faceMeshRenderable = ModelRenderable.builder().setSource(
                    checkNotNull<RenderableDefinition>(
                        faceMeshDefinition
                    )
                ).build().get()
                faceMeshRenderable!!.setRenderPriority(FACE_MESH_RENDER_PRIORITY)
            } catch (ex: InterruptedException) {
                Log.e(
                    TAG,
                    "Failed to build faceMeshRenderable from definition",
                    ex
                )
            } catch (ex: ExecutionException) {
                Log.e(
                    TAG,
                    "Failed to build faceMeshRenderable from definition",
                    ex
                )
            }
            checkNotNull<ModelRenderable>(faceMeshRenderable)
                .setShadowReceiver(false)
            checkNotNull<ModelRenderable>(faceMeshRenderable)
                .setShadowCaster(false)
            faceMeshNode.renderable = faceMeshRenderable
        } else {
            // Face mesh renderable already exists, so update it to match the face mesh definition.
            faceMeshRenderable!!.updateFromDefinition(
                checkNotNull<RenderableDefinition>(
                    faceMeshDefinition
                )
            )
        }
    }

    private fun updateFaceMeshVerticesAndTriangles() {
        val augmentedFace: AugmentedFace = checkNotNull<AugmentedFace>(
            augmentedFace
        )
        val verticesBuffer = augmentedFace.meshVertices
        verticesBuffer.rewind()
        // Vertices in x, y, z packing.
        val numVertices = verticesBuffer.limit() / 3
        val textureCoordsBuffer = augmentedFace.meshTextureCoordinates
        textureCoordsBuffer.rewind()
        // Texture coordinates in u, v packing.
        val numTextureCoords = textureCoordsBuffer.limit() / 2
        val normalsBuffer = augmentedFace.meshNormals
        normalsBuffer.rewind()
        // Normals in x, y, z packing.
        val numNormals = normalsBuffer.limit() / 3
        check(!(numVertices != numTextureCoords || numVertices != numNormals)) { "AugmentedFace must have the same number of vertices, normals, and texture coordinates." }
        vertices.ensureCapacity(numVertices)
        for (i in 0 until numVertices) {
            // position.
            val vX = verticesBuffer.get()
            val vY = verticesBuffer.get()
            val vZ = verticesBuffer.get()

            // Normal.
            val nX = normalsBuffer.get()
            val nY = normalsBuffer.get()
            val nZ = normalsBuffer.get()

            // Uv coordinate.
            val u = textureCoordsBuffer.get()
            val v = textureCoordsBuffer.get()
            if (i < vertices.size) {
                // Re-use existing vertex.
                val vertex = vertices[i]
                val vertexPos: Vector3 =
                    checkNotNull<Vector3>(vertex.position)
                vertexPos[vX, vY] = vZ
                val normal: Vector3 =
                    checkNotNull<Vector3>(vertex.normal)
                normal[nX, nY] = nZ
                val uvCoord: UvCoordinate =
                    checkNotNull<UvCoordinate>(vertex.uvCoordinate)
                uvCoord.x = u
                uvCoord.y = v
            } else {
                // Create new vertex.
                val vertex = Vertex.builder()
                    .setPosition(Vector3(vX, vY, vZ))
                    .setNormal(Vector3(nX, nY, nZ))
                    .setUvCoordinate(UvCoordinate(u, v))
                    .build()
                vertices.add(vertex)
            }
        }

        // Remove any extra vertices. In practice, this shouldn't happen.
        // The number of vertices remains the same each frame.
        while (vertices.size > numVertices) {
            vertices.removeAt(vertices.size - 1)
        }
        val indicesBuffer = augmentedFace.meshTriangleIndices
        indicesBuffer.rewind()

        // Only do this if the size doesn't match.
        // The triangle indices of the face mesh don't change from frame to frame.
        if (triangleIndices.size != indicesBuffer.limit()) {
            triangleIndices.clear()
            triangleIndices.ensureCapacity(indicesBuffer.limit())
            while (indicesBuffer.hasRemaining()) {
                triangleIndices.add(indicesBuffer.get().toInt())
            }
        }
    }

    private fun updateSubmeshes() {
        val currentFaceMeshMaterial =
            faceMeshMaterial
        if (defaultFaceMeshMaterial == null || currentFaceMeshMaterial == null) {
            return
        }
        val faceMeshMaterial: Material =
            checkNotNull<Material>(currentFaceMeshMaterial)
        val faceMeshOccluderMaterial: Material = checkNotNull<Material>(
            faceMeshOccluderMaterial
        )
        submeshes.clear()
        val occluderSubmesh = Submesh.builder()
            .setTriangleIndices(triangleIndices)
            .setMaterial(faceMeshOccluderMaterial)
            .build()
        submeshes.add(occluderSubmesh)
        if (faceMeshTexture != null) {
            if (faceMeshMaterial === defaultFaceMeshMaterial) {
                faceMeshMaterial.setTexture(
                    FACE_MESH_TEXTURE_MATERIAL_PARAMETER,
                    faceMeshTexture
                )
            }
            val faceTextureSubmesh = Submesh.builder()
                .setTriangleIndices(triangleIndices)
                .setMaterial(faceMeshMaterial)
                .build()
            submeshes.add(faceTextureSubmesh)
        }
    }

    private val faceMeshMaterial: Material?
        private get() = if (overrideFaceMeshMaterial != null) {
            overrideFaceMeshMaterial
        } else defaultFaceMeshMaterial

    companion object {
        private val TAG = AugmentedFaceNode::class.java.simpleName
        private const val FACE_MESH_TEXTURE_MATERIAL_PARAMETER = "texture"

        // Used to help ensure that the face mesh texture is rendered below the face mesh regions.
        // This helps prevent z-sorting issues with transparent materials.
        private val FACE_MESH_RENDER_PRIORITY = Renderable.RENDER_PRIORITY_LAST
            //Math.max(Renderable.RENDER_PRIORITY_FIRST, Renderable.RENDER_PRIORITY_DEFAULT - 1)

        private fun boneNameForRegion(regionType: RegionType): String {
            return regionType.name
        }

        private fun <T> checkNotNull(reference: T?): T {
            if (reference == null) {
                throw NullPointerException()
            }
            return reference
        }
    }

    /** Create an AugmentedFaceNode with no AugmentedFace.  */
    init {
        faceMeshNode = Node()
        faceMeshNode.setParent(this)
        faceRegionsSkeletonNode = SkeletonNode()
        faceRegionsSkeletonNode.setParent(this)
        for (regionType in RegionType.values()) {
            val regionNode = Node()
            regionNode.setParent(faceRegionsSkeletonNode)
            faceRegionsSkeletonNode.setBoneAttachment(
                boneNameForRegion(
                    regionType
                ), regionNode
            )
        }
        faceMeshDefinition =
            RenderableDefinition.builder().setVertices(vertices).setSubmeshes(submeshes).build()
    }
}