package com.example.zrenie20.renderable.alpha

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLDebugHelper
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import com.alphamovie.lib.GLTextureView
import com.alphamovie.lib.GLTextureView.*
import java.io.Writer
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.lang.ref.WeakReference
import java.util.ArrayList
import javax.microedition.khronos.egl.*
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10

open class MGLTextureView(
    val mTextureView: TextureView
    ) : SurfaceTextureListener, View.OnLayoutChangeListener {

    private val mDebugFlags = 0
    val mPreserveEGLContextOnPause = false

    @Throws(Throwable::class)
    protected open fun finalize() {
        try {
            if (mGLThread != null) {
                // GLThread may still be running if this view was never
                // attached to a window.
                mGLThread!!.requestExitAndWait()
            }
        } finally {
            //super.finalize()
        }
    }

    init {
        mTextureView.surfaceTextureListener = this
    }
    /**
     * Set the glWrapper. If the glWrapper is not null, its
     * [GLWrapper.wrap] method is called
     * whenever a surface is created. A GLWrapper can be used to wrap
     * the GL object that's passed to the renderer. Wrapping a GL
     * object enables examining and modifying the behavior of the
     * GL calls made by the renderer.
     *
     *
     * Wrapping is typically used for debugging purposes.
     *
     *
     * The default value is null.
     * @param glWrapper the new GLWrapper
     */
    fun setGLWrapper(glWrapper: MGLTextureView.GLWrapper?) {
        mGLWrapper = glWrapper
    }

    /**
     * Set the renderer associated with this view. Also starts the thread that
     * will call the renderer, which in turn causes the rendering to start.
     *
     * This method should be called once and only once in the life-cycle of
     * a MGLTextureView.
     *
     * The following MGLTextureView methods can only be called *before*
     * setRenderer is called:
     *
     *  * [.setEGLConfigChooser]
     *  * [.setEGLConfigChooser]
     *  * [.setEGLConfigChooser]
     *
     *
     *
     * The following MGLTextureView methods can only be called *after*
     * setRenderer is called:
     *
     *  * [.getRenderMode]
     *  * [.onPause]
     *  * [.onResume]
     *  * [.queueEvent]
     *  * [.requestRender]
     *  * [.setRenderMode]
     *
     *
     * @param renderer the renderer to use to perform OpenGL drawing.
     */
    fun setRenderer(renderer: MGLTextureView.Renderer?) {
        checkRenderThreadState()
        if (mEGLConfigChooser == null) {
            mEGLConfigChooser = SimpleEGLConfigChooser(true)
        }
        if (mEGLContextFactory == null) {
            mEGLContextFactory = DefaultContextFactory()
        }
        if (mEGLWindowSurfaceFactory == null) {
            mEGLWindowSurfaceFactory = DefaultWindowSurfaceFactory()
        }
        mRenderer = renderer
        mGLThread = GLThread(mThisWeakRef)
        mGLThread!!.start()
    }

    /**
     * Install a custom EGLContextFactory.
     *
     * If this method is
     * called, it must be called before [.setRenderer]
     * is called.
     *
     *
     * If this method is not called, then by default
     * a context will be created with no shared context and
     * with a null attribute list.
     */
    fun setEGLContextFactory(factory: MGLTextureView.EGLContextFactory?) {
        checkRenderThreadState()
        mEGLContextFactory = factory
    }

    /**
     * Install a custom EGLWindowSurfaceFactory.
     *
     * If this method is
     * called, it must be called before [.setRenderer]
     * is called.
     *
     *
     * If this method is not called, then by default
     * a window surface will be created with a null attribute list.
     */
    fun setEGLWindowSurfaceFactory(factory: MGLTextureView.EGLWindowSurfaceFactory?) {
        checkRenderThreadState()
        mEGLWindowSurfaceFactory = factory
    }

    /**
     * Install a custom EGLConfigChooser.
     *
     * If this method is
     * called, it must be called before [.setRenderer]
     * is called.
     *
     *
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose an EGLConfig that is compatible with the current
     * android.view.Surface, with a depth buffer depth of
     * at least 16 bits.
     * @param configChooser
     */
    fun setEGLConfigChooser(configChooser: MGLTextureView.EGLConfigChooser?) {
        checkRenderThreadState()
        mEGLConfigChooser = configChooser
    }

    /**
     * Install a config chooser which will choose a config
     * as close to 16-bit RGB as possible, with or without an optional depth
     * buffer as close to 16-bits as possible.
     *
     * If this method is
     * called, it must be called before [.setRenderer]
     * is called.
     *
     *
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose an RGB_888 surface with a depth buffer depth of
     * at least 16 bits.
     *
     * @param needDepth
     */
    fun setEGLConfigChooser(needDepth: Boolean) {
        setEGLConfigChooser(SimpleEGLConfigChooser(needDepth))
    }

    /**
     * Install a config chooser which will choose a config
     * with at least the specified depthSize and stencilSize,
     * and exactly the specified redSize, greenSize, blueSize and alphaSize.
     *
     * If this method is
     * called, it must be called before [.setRenderer]
     * is called.
     *
     *
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose an RGB_888 surface with a depth buffer depth of
     * at least 16 bits.
     *
     */
    fun setEGLConfigChooser(
        redSize: Int, greenSize: Int, blueSize: Int,
        alphaSize: Int, depthSize: Int, stencilSize: Int
    ) {
        setEGLConfigChooser(
            ComponentSizeChooser(
                redSize, greenSize,
                blueSize, alphaSize, depthSize, stencilSize
            )
        )
    }

    /**
     * Inform the default EGLContextFactory and default EGLConfigChooser
     * which EGLContext client version to pick.
     *
     * Use this method to create an OpenGL ES 2.0-compatible context.
     * Example:
     * <pre class="prettyprint">
     * public MyView(Context context) {
     * super(context);
     * setEGLContextClientVersion(2); // Pick an OpenGL ES 2.0 context.
     * setRenderer(new MyRenderer());
     * }
    </pre> *
     *
     * Note: Activities which require OpenGL ES 2.0 should indicate this by
     * setting @lt;uses-feature android:glEsVersion="0x00020000" /> in the activity's
     * AndroidManifest.xml file.
     *
     * If this method is called, it must be called before [.setRenderer]
     * is called.
     *
     * This method only affects the behavior of the default EGLContexFactory and the
     * default EGLConfigChooser. If
     * [.setEGLContextFactory] has been called, then the supplied
     * EGLContextFactory is responsible for creating an OpenGL ES 2.0-compatible context.
     * If
     * [.setEGLConfigChooser] has been called, then the supplied
     * EGLConfigChooser is responsible for choosing an OpenGL ES 2.0-compatible config.
     * @param version The EGLContext client version to choose. Use 2 for OpenGL ES 2.0
     */
    fun setEGLContextClientVersion(version: Int) {
        checkRenderThreadState()
        mEGLContextClientVersion = version
    }
    /**
     * Get the current rendering mode. May be called
     * from any thread. Must not be called before a renderer has been set.
     * @return the current rendering mode.
     * @see .RENDERMODE_CONTINUOUSLY
     *
     * @see .RENDERMODE_WHEN_DIRTY
     */
    /**
     * Set the rendering mode. When renderMode is
     * RENDERMODE_CONTINUOUSLY, the renderer is called
     * repeatedly to re-render the scene. When renderMode
     * is RENDERMODE_WHEN_DIRTY, the renderer only rendered when the surface
     * is created, or when [.requestRender] is called. Defaults to RENDERMODE_CONTINUOUSLY.
     *
     *
     * Using RENDERMODE_WHEN_DIRTY can improve battery life and overall system performance
     * by allowing the GPU and CPU to idle when the view does not need to be updated.
     *
     *
     * This method can only be called after [.setRenderer]
     *
     * @param renderMode one of the RENDERMODE_X constants
     * @see .RENDERMODE_CONTINUOUSLY
     *
     * @see .RENDERMODE_WHEN_DIRTY
     */
    var renderMode: Int
        get() = mGLThread!!.renderMode
        set(renderMode) {
            mGLThread!!.renderMode = renderMode
        }

    /**
     * Request that the renderer render a frame.
     * This method is typically used when the render mode has been set to
     * [.RENDERMODE_WHEN_DIRTY], so that frames are only rendered on demand.
     * May be called
     * from any thread. Must not be called before a renderer has been set.
     */
    fun requestRender() {
        mGLThread!!.requestRender()
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of MGLTextureView.
     */
    fun surfaceCreated(texture: SurfaceTexture?) {
        mGLThread!!.surfaceCreated()
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of MGLTextureView.
     */
    fun surfaceDestroyed(texture: SurfaceTexture?) {
        // Surface will be destroyed when we return
        mGLThread!!.surfaceDestroyed()
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of MGLTextureView.
     */
    fun surfaceChanged(texture: SurfaceTexture?, format: Int, w: Int, h: Int) {
        mGLThread!!.onWindowResize(w, h)
    }

    /**
     * Inform the view that the activity is paused. The owner of this view must
     * call this method when the activity is paused. Calling this method will
     * pause the rendering thread.
     * Must not be called before a renderer has been set.
     */
    open fun onPause() {
        mGLThread!!.onPause()
    }

    /**
     * Inform the view that the activity is resumed. The owner of this view must
     * call this method when the activity is resumed. Calling this method will
     * recreate the OpenGL display and resume the rendering
     * thread.
     * Must not be called before a renderer has been set.
     */
    open fun onResume() {
        mGLThread!!.onResume()
    }

    /**
     * Queue a runnable to be run on the GL rendering thread. This can be used
     * to communicate with the Renderer on the rendering thread.
     * Must not be called before a renderer has been set.
     * @param r the runnable to be run on the GL rendering thread.
     */
    fun queueEvent(r: Runnable?) {
        mGLThread!!.queueEvent(r)
    }

    /**
     * This method is used as part of the View class and is not normally
     * called or subclassed by clients of MGLTextureView.
     */
    /*override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (LOG_ATTACH_DETACH) {
            Log.d(
                TAG,
                "onAttachedToWindow reattach =$mDetached"
            )
        }
        if (mDetached && mRenderer != null) {
            var renderMode: Int = RENDERMODE_CONTINUOUSLY
            if (mGLThread != null) {
                renderMode = mGLThread!!.renderMode
            }
            mGLThread = GLThread(mThisWeakRef)
            if (renderMode != RENDERMODE_CONTINUOUSLY) {
                mGLThread!!.renderMode = renderMode
            }
            mGLThread!!.start()
        }
        mDetached = false
    }*/

    /**
     * This method is used as part of the View class and is not normally
     * called or subclassed by clients of MGLTextureView.
     * Must not be called before a renderer has been set.
     */
    /*override fun onDetachedFromWindow() {
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onDetachedFromWindow")
        }
        if (mGLThread != null) {
            mGLThread!!.requestExitAndWait()
        }
        mDetached = true

        super.onDetachedFromWindow()
    }*/

    override fun onLayoutChange(
        v: View, left: Int, top: Int, right: Int, bottom: Int,
        oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
    ) {
        surfaceChanged(mTextureView.surfaceTexture, 0, right - left, bottom - top)
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (LOG_ATTACH_DETACH) {
            Log.d(
                TAG,
                "onAttachedToWindow reattach =$mDetached"
            )
        }
        if (mDetached && mRenderer != null) {
            var renderMode: Int = RENDERMODE_CONTINUOUSLY
            if (mGLThread != null) {
                renderMode = mGLThread!!.renderMode
            }
            mGLThread = GLThread(mThisWeakRef)
            if (renderMode != RENDERMODE_CONTINUOUSLY) {
                mGLThread!!.renderMode = renderMode
            }
            mGLThread!!.start()
        }
        mDetached = false

        surfaceCreated(surface)
        surfaceChanged(surface, 0, width, height)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        surfaceChanged(surface, 0, width, height)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        surfaceDestroyed(surface)

        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onDetachedFromWindow")
        }
        if (mGLThread != null) {
            mGLThread!!.requestExitAndWait()
        }
        mDetached = true

        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        requestRender()
    }
    // ----------------------------------------------------------------------
    /**
     * An interface used to wrap a GL interface.
     *
     * Typically
     * used for implementing debugging and tracing on top of the default
     * GL interface. You would typically use this by creating your own class
     * that implemented all the GL methods by delegating to another GL instance.
     * Then you could add your own behavior before or after calling the
     * delegate. All the GLWrapper would do was instantiate and return the
     * wrapper GL instance:
     * <pre class="prettyprint">
     * class MyGLWrapper implements GLWrapper {
     * GL wrap(GL gl) {
     * return new MyGLImplementation(gl);
     * }
     * static class MyGLImplementation implements GL,GL10,GL11,... {
     * ...
     * }
     * }
    </pre> *
     * @see .setGLWrapper
     */
    interface GLWrapper {
        /**
         * Wraps a gl interface in another gl interface.
         * @param gl a GL interface that is to be wrapped.
         * @return either the input argument or another GL object that wraps the input argument.
         */
        fun wrap(gl: GL?): GL?
    }

    /**
     * A generic renderer interface.
     *
     *
     * The renderer is responsible for making OpenGL calls to render a frame.
     *
     *
     * MGLTextureView clients typically create their own classes that implement
     * this interface, and then call [GLTextureView.setRenderer] to
     * register the renderer with the MGLTextureView.
     *
     *
     *
     * <div class="special reference">
     * <h3>Developer Guides</h3>
     *
     * For more information about how to use OpenGL, read the
     * [OpenGL]({@docRoot}guide/topics/graphics/opengl.html) developer guide.
    </div> *
     *
     * <h3>Threading</h3>
     * The renderer will be called on a separate thread, so that rendering
     * performance is decoupled from the UI thread. Clients typically need to
     * communicate with the renderer from the UI thread, because that's where
     * input events are received. Clients can communicate using any of the
     * standard Java techniques for cross-thread communication, or they can
     * use the [GLTextureView.queueEvent] convenience method.
     *
     *
     * <h3>EGL Context Lost</h3>
     * There are situations where the EGL rendering context will be lost. This
     * typically happens when device wakes up after going to sleep. When
     * the EGL context is lost, all OpenGL resources (such as textures) that are
     * associated with that context will be automatically deleted. In order to
     * keep rendering correctly, a renderer must recreate any lost resources
     * that it still needs. The [.onSurfaceCreated] method
     * is a convenient place to do this.
     *
     *
     * @see .setRenderer
     */
    interface Renderer {
        /**
         * Called when the surface is created or recreated.
         *
         *
         * Called when the rendering thread
         * starts and whenever the EGL context is lost. The EGL context will typically
         * be lost when the Android device awakes after going to sleep.
         *
         *
         * Since this method is called at the beginning of rendering, as well as
         * every time the EGL context is lost, this method is a convenient place to put
         * code to create resources that need to be created when the rendering
         * starts, and that need to be recreated when the EGL context is lost.
         * Textures are an example of a resource that you might want to create
         * here.
         *
         *
         * Note that when the EGL context is lost, all OpenGL resources associated
         * with that context will be automatically deleted. You do not need to call
         * the corresponding "glDelete" methods such as glDeleteTextures to
         * manually delete these lost resources.
         *
         *
         * @param gl the GL interface. Use `instanceof` to
         * test if the interface supports GL11 or higher interfaces.
         * @param config the EGLConfig of the created surface. Can be used
         * to create matching pbuffers.
         */
        fun onSurfaceCreated(gl: GL10?, config: EGLConfig?)

        /**
         * Called when the surface changed size.
         *
         *
         * Called after the surface is created and whenever
         * the OpenGL ES surface size changes.
         *
         *
         * Typically you will set your viewport here. If your camera
         * is fixed then you could also set your projection matrix here:
         * <pre class="prettyprint">
         * void onSurfaceChanged(GL10 gl, int width, int height) {
         * gl.glViewport(0, 0, width, height);
         * // for a fixed camera, set the projection too
         * float ratio = (float) width / height;
         * gl.glMatrixMode(GL10.GL_PROJECTION);
         * gl.glLoadIdentity();
         * gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
         * }
        </pre> *
         * @param gl the GL interface. Use `instanceof` to
         * test if the interface supports GL11 or higher interfaces.
         * @param width
         * @param height
         */
        fun onSurfaceChanged(gl: GL10?, width: Int, height: Int)

        /**
         * Called to draw the current frame.
         *
         *
         * This method is responsible for drawing the current frame.
         *
         *
         * The implementation of this method typically looks like this:
         * <pre class="prettyprint">
         * void onDrawFrame(GL10 gl) {
         * gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
         * //... other gl calls to render the scene ...
         * }
        </pre> *
         * @param gl the GL interface. Use `instanceof` to
         * test if the interface supports GL11 or higher interfaces.
         */
        fun onDrawFrame(gl: GL10?)
        fun onSurfaceDestroyed(gl: GL10?)
    }

    /**
     * An interface for customizing the eglCreateContext and eglDestroyContext calls.
     *
     *
     * This interface must be implemented by clients wishing to call
     * [GLTextureView.setEGLContextFactory]
     */
    interface EGLContextFactory {
        fun createContext(egl: EGL10?, display: EGLDisplay?, eglConfig: EGLConfig?): EGLContext?
        fun destroyContext(egl: EGL10?, display: EGLDisplay?, context: EGLContext?)
    }

    private inner class DefaultContextFactory : MGLTextureView.EGLContextFactory {
        private val EGL_CONTEXT_CLIENT_VERSION = 0x3098

        override fun createContext(
            egl: EGL10?,
            display: EGLDisplay?,
            eglConfig: EGLConfig?
        ): EGLContext? {
            val attrib_list = intArrayOf(
                EGL_CONTEXT_CLIENT_VERSION,
                mEGLContextClientVersion,
                EGL10.EGL_NONE
            )
            return egl?.eglCreateContext(
                display, eglConfig, EGL10.EGL_NO_CONTEXT,
                if (mEGLContextClientVersion != 0) attrib_list else null
            )
        }

        override fun destroyContext(egl: EGL10?, display: EGLDisplay?, context: EGLContext?) {
            if (egl?.eglDestroyContext(display, context) != true) {
                Log.e("DefaultContextFactory", "display:$display context: $context")
                if (LOG_THREADS) {
                    Log.i("DefaultContextFactory", "tid=" + Thread.currentThread().id)
                }
                EglHelper.throwEglException("eglDestroyContex", egl!!.eglGetError())
            }
        }
    }

    /**
     * An interface for customizing the eglCreateWindowSurface and eglDestroySurface calls.
     *
     *
     * This interface must be implemented by clients wishing to call
     * [GLTextureView.setEGLWindowSurfaceFactory]
     */
    interface EGLWindowSurfaceFactory {
        /**
         * @return null if the surface cannot be constructed.
         */
        fun createWindowSurface(
            egl: EGL10?, display: EGLDisplay?, config: EGLConfig?,
            nativeWindow: Any?
        ): EGLSurface?

        fun destroySurface(egl: EGL10?, display: EGLDisplay?, surface: EGLSurface?)
    }

    private class DefaultWindowSurfaceFactory : MGLTextureView.EGLWindowSurfaceFactory {
        override fun createWindowSurface(
            egl: EGL10?,
            display: EGLDisplay?,
            config: EGLConfig?,
            nativeWindow: Any?
        ): EGLSurface? {

            var result: EGLSurface? = null
            try {
                result = egl?.eglCreateWindowSurface(display, config, nativeWindow, null)
            } catch (e: IllegalArgumentException) {
                // This exception indicates that the surface flinger surface
                // is not valid. This can happen if the surface flinger surface has
                // been torn down, but the application has not yet been
                // notified via SurfaceHolder.Callback.surfaceDestroyed.
                // In theory the application should be notified first,
                // but in practice sometimes it is not. See b/4588890
                Log.e(TAG, "eglCreateWindowSurface", e)
            }
            return result
        }

        override fun destroySurface(egl: EGL10?, display: EGLDisplay?, surface: EGLSurface?) {
            egl?.eglDestroySurface(display, surface)
        }
    }

    /**
     * An interface for choosing an EGLConfig configuration from a list of
     * potential configurations.
     *
     *
     * This interface must be implemented by clients wishing to call
     * [GLTextureView.setEGLConfigChooser]
     */
    interface EGLConfigChooser {
        /**
         * Choose a configuration from the list. Implementors typically
         * implement this method by calling
         * [EGL10.eglChooseConfig] and iterating through the results. Please consult the
         * EGL specification available from The Khronos Group to learn how to call eglChooseConfig.
         * @param egl the EGL10 for the current display.
         * @param display the current display.
         * @return the chosen configuration.
         */
        fun chooseConfig(egl: EGL10?, display: EGLDisplay?): EGLConfig?
    }

    abstract inner class BaseConfigChooser(configSpec: IntArray) :
        MGLTextureView.EGLConfigChooser {

        override fun chooseConfig(egl: EGL10?, display: EGLDisplay?): EGLConfig? {

            val num_config = IntArray(1)
            require(
                egl!!.eglChooseConfig(
                    display, mConfigSpec, null, 0,
                    num_config
                )
            ) { "eglChooseConfig failed" }
            val numConfigs = num_config[0]
            require(numConfigs > 0) { "No configs match configSpec" }
            val configs = arrayOfNulls<EGLConfig>(numConfigs)
            require(
                egl!!.eglChooseConfig(
                    display, mConfigSpec, configs, numConfigs,
                    num_config
                )
            ) { "eglChooseConfig#2 failed" }
            return chooseConfig(egl, display, configs)
                ?: throw IllegalArgumentException("No config chosen")
        }

        abstract fun chooseConfig(
            egl: EGL10?, display: EGLDisplay?,
            configs: Array<EGLConfig?>?
        ): EGLConfig?

        protected var mConfigSpec: IntArray
        private fun filterConfigSpec(configSpec: IntArray): IntArray {
            if (mEGLContextClientVersion != 2) {
                return configSpec
            }
            /* We know none of the subclasses define EGL_RENDERABLE_TYPE.
             * And we know the configSpec is well formed.
             */
            val len = configSpec.size
            val newConfigSpec = IntArray(len + 2)
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1)
            newConfigSpec[len - 1] = EGL10.EGL_RENDERABLE_TYPE
            newConfigSpec[len] = 4 /* EGL_OPENGL_ES2_BIT */
            newConfigSpec[len + 1] = EGL10.EGL_NONE
            return newConfigSpec
        }

        init {
            mConfigSpec = filterConfigSpec(configSpec)
        }
    }

    /**
     * Choose a configuration with exactly the specified r,g,b,a sizes,
     * and at least the specified depth and stencil sizes.
     */
     open inner class ComponentSizeChooser(
        redSize: Int, greenSize: Int, blueSize: Int,
        alphaSize: Int, depthSize: Int, stencilSize: Int
    ) :
        BaseConfigChooser(
            intArrayOf(
                EGL10.EGL_RED_SIZE, redSize,
                EGL10.EGL_GREEN_SIZE, greenSize,
                EGL10.EGL_BLUE_SIZE, blueSize,
                EGL10.EGL_ALPHA_SIZE, alphaSize,
                EGL10.EGL_DEPTH_SIZE, depthSize,
                EGL10.EGL_STENCIL_SIZE, stencilSize,
                EGL10.EGL_NONE
            )
        ) {

        override fun chooseConfig(
            egl: EGL10?,
            display: EGLDisplay?,
            configs: Array<EGLConfig?>?
        ): EGLConfig? {

            if (egl == null ||
                display == null ||
                configs == null
            ) {
                return null
            }

            for (config in configs) {
                config!!

                val d = findConfigAttrib(
                    egl, display, config,
                    EGL10.EGL_DEPTH_SIZE, 0
                )
                val s = findConfigAttrib(
                    egl, display, config,
                    EGL10.EGL_STENCIL_SIZE, 0
                )
                if (d >= mDepthSize && s >= mStencilSize) {
                    val r = findConfigAttrib(
                        egl, display, config,
                        EGL10.EGL_RED_SIZE, 0
                    )
                    val g = findConfigAttrib(
                        egl, display, config,
                        EGL10.EGL_GREEN_SIZE, 0
                    )
                    val b = findConfigAttrib(
                        egl, display, config,
                        EGL10.EGL_BLUE_SIZE, 0
                    )
                    val a = findConfigAttrib(
                        egl, display, config,
                        EGL10.EGL_ALPHA_SIZE, 0
                    )
                    if (r == mRedSize && g == mGreenSize
                        && b == mBlueSize && a == mAlphaSize
                    ) {
                        return config
                    }
                }
            }
            return null
        }

        private fun findConfigAttrib(
            egl: EGL10, display: EGLDisplay,
            config: EGLConfig, attribute: Int, defaultValue: Int
        ): Int {
            return if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                mValue[0]
            } else defaultValue
        }

        private val mValue: IntArray

        // Subclasses can adjust these values:
        protected var mRedSize: Int
        protected var mGreenSize: Int
        protected var mBlueSize: Int
        protected var mAlphaSize: Int
        protected var mDepthSize: Int
        protected var mStencilSize: Int

        init {
            mValue = IntArray(1)
            mRedSize = redSize
            mGreenSize = greenSize
            mBlueSize = blueSize
            mAlphaSize = alphaSize
            mDepthSize = depthSize
            mStencilSize = stencilSize
        }
    }

    /**
     * This class will choose a RGB_888 surface with
     * or without a depth buffer.
     *
     */
    private inner class SimpleEGLConfigChooser(withDepthBuffer: Boolean) :
        ComponentSizeChooser(8, 8, 8, 0, if (withDepthBuffer) 16 else 0, 0)

    /**
     * An EGL helper class.
     */
    private class EglHelper(private val mGLSurfaceViewWeakRef: WeakReference<MGLTextureView?>) {
        /**
         * Initialize EGL for a given configuration spec.
         * @param
         */
        fun start() {
            if (LOG_EGL) {
                Log.w("EglHelper", "start() tid=" + Thread.currentThread().id)
            }
            /*
             * Get an EGL instance
             */mEgl = EGLContext.getEGL() as EGL10

            /*
             * Get to the default display.
             */mEglDisplay = mEgl!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
            if (mEglDisplay === EGL10.EGL_NO_DISPLAY) {
                throw RuntimeException("eglGetDisplay failed")
            }

            /*
             * We can now initialize EGL for that display
             */
            val version = IntArray(2)
            if (!mEgl!!.eglInitialize(mEglDisplay, version)) {
                throw RuntimeException("eglInitialize failed")
            }
            val view = mGLSurfaceViewWeakRef.get()
            if (view == null) {
                mEglConfig = null
                mEglContext = null
            } else {
                mEglConfig = view.mEGLConfigChooser?.chooseConfig(mEgl, mEglDisplay)

                /*
                * Create an EGL context. We want to do this as rarely as we can, because an
                * EGL context is a somewhat heavy object.
                */mEglContext = view.mEGLContextFactory?.createContext(mEgl, mEglDisplay, mEglConfig)
            }
            if (mEglContext == null || mEglContext === EGL10.EGL_NO_CONTEXT) {
                mEglContext = null
                throwEglException("createContext")
            }
            if (LOG_EGL) {
                Log.w(
                    "EglHelper",
                    "createContext " + mEglContext + " tid=" + Thread.currentThread().id
                )
            }
            mEglSurface = null
        }

        /**
         * Create an egl surface for the current SurfaceHolder surface. If a surface
         * already exists, destroy it before creating the new surface.
         *
         * @return true if the surface was created successfully.
         */
        fun createSurface(): Boolean {
            if (LOG_EGL) {
                Log.w("EglHelper", "createSurface()  tid=" + Thread.currentThread().id)
            }
            /*
             * Check preconditions.
             */if (mEgl == null) {
                throw RuntimeException("egl not initialized")
            }
            if (mEglDisplay == null) {
                throw RuntimeException("eglDisplay not initialized")
            }
            if (mEglConfig == null) {
                throw RuntimeException("mEglConfig not initialized")
            }

            /*
             *  The window size has changed, so we need to create a new
             *  surface.
             */destroySurfaceImp()

            /*
             * Create an EGL surface we can render into.
             */
            val view = mGLSurfaceViewWeakRef.get()
            mEglSurface = view?.mEGLWindowSurfaceFactory?.createWindowSurface(
                mEgl,
                mEglDisplay, mEglConfig, view.mTextureView.surfaceTexture
            )
            if (mEglSurface == null || mEglSurface === EGL10.EGL_NO_SURFACE) {
                val error = mEgl!!.eglGetError()
                if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                    Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.")
                }
                return false
            }

            /*
             * Before we can issue GL commands, we need to make sure
             * the context is current and bound to a surface.
             */if (!mEgl!!.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
                /*
                 * Could not make the context current, probably because the underlying
                 * SurfaceView surface has been destroyed.
                 */
                logEglErrorAsWarning("EGLHelper", "eglMakeCurrent", mEgl!!.eglGetError())
                return false
            }
            return true
        }

        /**
         * Create a GL object for the current EGL context.
         * @return
         */
        fun createGL(): GL {
            var gl = mEglContext!!.gl
            val view = mGLSurfaceViewWeakRef.get()
            if (view != null) {
                if (view.mGLWrapper != null) {
                    gl = view.mGLWrapper!!.wrap(gl)
                }

                /*if (view.mDebugFlags and (DEBUG_CHECK_GL_ERROR or DEBUG_LOG_GL_CALLS) != 0) {
                    var configFlags = 0
                    var log: Writer? = null
                    if (view.mDebugFlags and DEBUG_CHECK_GL_ERROR != 0) {
                        configFlags = configFlags or GLDebugHelper.CONFIG_CHECK_GL_ERROR
                    }
                    if (view.mDebugFlags and DEBUG_LOG_GL_CALLS != 0) {
                        log = MGLTextureView.LogWriter()
                    }
                    gl = GLDebugHelper.wrap(gl, configFlags, log)
                }*/
            }
            return gl
        }

        /**
         * Display the current render surface.
         * @return the EGL error code from eglSwapBuffers.
         */
        fun swap(): Int {
            return if (!mEgl!!.eglSwapBuffers(mEglDisplay, mEglSurface)) {
                mEgl!!.eglGetError()
            } else EGL10.EGL_SUCCESS
        }

        fun destroySurface() {
            if (LOG_EGL) {
                Log.w("EglHelper", "destroySurface()  tid=" + Thread.currentThread().id)
            }
            destroySurfaceImp()
        }

        private fun destroySurfaceImp() {
            if (mEglSurface != null && mEglSurface !== EGL10.EGL_NO_SURFACE) {
                mEgl!!.eglMakeCurrent(
                    mEglDisplay, EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_CONTEXT
                )
                val view = mGLSurfaceViewWeakRef.get()
                view?.mEGLWindowSurfaceFactory?.destroySurface(mEgl, mEglDisplay, mEglSurface)
                mEglSurface = null
            }
        }

        fun finish() {
            if (LOG_EGL) {
                Log.w("EglHelper", "finish() tid=" + Thread.currentThread().id)
            }
            if (mEglContext != null) {
                val view = mGLSurfaceViewWeakRef.get()
                view?.mEGLContextFactory?.destroyContext(mEgl, mEglDisplay, mEglContext)
                mEglContext = null
            }
            if (mEglDisplay != null) {
                mEgl!!.eglTerminate(mEglDisplay)
                mEglDisplay = null
            }
        }

        private fun throwEglException(function: String) {
            throwEglException(function, mEgl!!.eglGetError())
        }

        var mEgl: EGL10? = null
        var mEglDisplay: EGLDisplay? = null
        var mEglSurface: EGLSurface? = null
        var mEglConfig: EGLConfig? = null
        var mEglContext: EGLContext? = null

        companion object {
            fun throwEglException(function: String, error: Int) {
                val message = formatEglError(function, error)
                if (LOG_THREADS) {
                    Log.e(
                        "EglHelper", "throwEglException tid=" + Thread.currentThread().id + " "
                                + message
                    )
                }
                throw RuntimeException(message)
            }

            fun logEglErrorAsWarning(tag: String?, function: String, error: Int) {
                Log.w(tag, formatEglError(function, error))
            }

            fun formatEglError(function: String, error: Int): String {
                return "$function failed: $error"
            }
        }
    }

    /**
     * A generic GL Thread. Takes care of initializing EGL and GL. Delegates
     * to a Renderer instance to do the actual drawing. Can be configured to
     * render continuously or on request.
     *
     * All potentially blocking synchronization is done through the
     * sGLThreadManager object. This avoids multiple-lock ordering issues.
     *
     */
    internal class GLThread(glSurfaceViewWeakRef: WeakReference<MGLTextureView?>) :
        Thread() {
        override fun run() {
            name = "GLThread $id"
            if (LOG_THREADS) {
                Log.i("GLThread", "starting tid=$id")
            }
            try {
                guardedRun()
            } catch (e: InterruptedException) {
                // fall thru and exit normally
            } finally {
                sGLThreadManager.threadExiting(this)
            }
        }

        /*
         * This private method should only be called inside a
         * synchronized(sGLThreadManager) block.
         */
        private fun stopEglSurfaceLocked() {
            if (mHaveEglSurface) {
                mHaveEglSurface = false
                mEglHelper!!.destroySurface()
            }
        }

        /*
         * This private method should only be called inside a
         * synchronized(sGLThreadManager) block.
         */
        private fun stopEglContextLocked() {
            if (mHaveEglContext) {
                mEglHelper!!.finish()
                mHaveEglContext = false
                sGLThreadManager.releaseEglContextLocked(this)
            }
        }

        @Throws(InterruptedException::class)
        private fun guardedRun() {
            mEglHelper = EglHelper(mGLSurfaceViewWeakRef)
            mHaveEglContext = false
            mHaveEglSurface = false
            try {
                var gl: GL10? = null
                var createEglContext = false
                var createEglSurface = false
                var createGlInterface = false
                var lostEglContext = false
                var sizeChanged = false
                var wantRenderNotification = false
                var doRenderNotification = false
                var askedToReleaseEglContext = false
                var w = 0
                var h = 0
                var event: Runnable? = null
                while (true) {
                    synchronized(sGLThreadManager) {
                        while (true) {
                            if (mShouldExit) {
                                return
                            }
                            if (!mEventQueue.isEmpty()) {
                                event = mEventQueue.removeAt(0)
                                break
                            }

                            // Update the pause state.
                            var pausing = false
                            if (mPaused != mRequestPaused) {
                                pausing = mRequestPaused
                                mPaused = mRequestPaused
                                sGLThreadManager.notifyAll()
                                if (LOG_PAUSE_RESUME) {
                                    Log.i(
                                        "GLThread",
                                        "mPaused is now " + mPaused + " tid=" + id
                                    )
                                }
                            }

                            // Do we need to give up the EGL context?
                            if (mShouldReleaseEglContext) {
                                if (LOG_SURFACE) {
                                    Log.i(
                                        "GLThread",
                                        "releasing EGL context because asked to tid=" + id
                                    )
                                }
                                stopEglSurfaceLocked()
                                stopEglContextLocked()
                                mShouldReleaseEglContext = false
                                askedToReleaseEglContext = true
                            }

                            // Have we lost the EGL context?
                            if (lostEglContext) {
                                stopEglSurfaceLocked()
                                stopEglContextLocked()
                                lostEglContext = false
                            }

                            // When pausing, release the EGL surface:
                            if (pausing && mHaveEglSurface) {
                                if (LOG_SURFACE) {
                                    Log.i(
                                        "GLThread",
                                        "releasing EGL surface because paused tid=" + id
                                    )
                                }
                                stopEglSurfaceLocked()
                            }

                            // When pausing, optionally release the EGL Context:
                            if (pausing && mHaveEglContext) {
                                val view =
                                    mGLSurfaceViewWeakRef.get()
                                val preserveEglContextOnPause =
                                    view?.mPreserveEGLContextOnPause ?: false
                                if (!preserveEglContextOnPause || sGLThreadManager.shouldReleaseEGLContextWhenPausing()) {
                                    stopEglContextLocked()
                                    if (LOG_SURFACE) {
                                        Log.i(
                                            "GLThread",
                                            "releasing EGL context because paused tid=" + id
                                        )
                                    }
                                }
                            }

                            // When pausing, optionally terminate EGL:
                            if (pausing) {
                                if (sGLThreadManager.shouldTerminateEGLWhenPausing()) {
                                    mEglHelper!!.finish()
                                    if (LOG_SURFACE) {
                                        Log.i(
                                            "GLThread",
                                            "terminating EGL because paused tid=" + id
                                        )
                                    }
                                }
                            }

                            // Have we lost the SurfaceView surface?
                            if (!mHasSurface && !mWaitingForSurface) {
                                if (LOG_SURFACE) {
                                    Log.i(
                                        "GLThread",
                                        "noticed surfaceView surface lost tid=" + id
                                    )
                                }
                                if (mHaveEglSurface) {
                                    stopEglSurfaceLocked()
                                }
                                mWaitingForSurface = true
                                mSurfaceIsBad = false
                                sGLThreadManager.notifyAll()
                            }

                            // Have we acquired the surface view surface?
                            if (mHasSurface && mWaitingForSurface) {
                                if (LOG_SURFACE) {
                                    Log.i(
                                        "GLThread",
                                        "noticed surfaceView surface acquired tid=" + id
                                    )
                                }
                                mWaitingForSurface = false
                                sGLThreadManager.notifyAll()
                            }
                            if (doRenderNotification) {
                                if (LOG_SURFACE) {
                                    Log.i(
                                        "GLThread",
                                        "sending render notification tid=" + id
                                    )
                                }
                                wantRenderNotification = false
                                doRenderNotification = false
                                mRenderComplete = true
                                sGLThreadManager.notifyAll()
                            }

                            // Ready to draw?
                            if (readyToDraw()) {

                                // If we don't have an EGL context, try to acquire one.
                                if (!mHaveEglContext) {
                                    if (askedToReleaseEglContext) {
                                        askedToReleaseEglContext = false
                                    } else if (sGLThreadManager.tryAcquireEglContextLocked(
                                            this
                                        )
                                    ) {
                                        try {
                                            mEglHelper!!.start()
                                        } catch (t: RuntimeException) {
                                            sGLThreadManager.releaseEglContextLocked(
                                                this
                                            )
                                            throw t
                                        }
                                        mHaveEglContext = true
                                        createEglContext = true
                                        sGLThreadManager.notifyAll()
                                    }
                                }
                                if (mHaveEglContext && !mHaveEglSurface) {
                                    mHaveEglSurface = true
                                    createEglSurface = true
                                    createGlInterface = true
                                    sizeChanged = true
                                }
                                if (mHaveEglSurface) {
                                    if (mSizeChanged) {
                                        sizeChanged = true
                                        w = mWidth
                                        h = mHeight
                                        wantRenderNotification = true
                                        if (LOG_SURFACE) {
                                            Log.i(
                                                "GLThread",
                                                "noticing that we want render notification tid="
                                                        + id
                                            )
                                        }

                                        // Destroy and recreate the EGL surface.
                                        createEglSurface = true
                                        mSizeChanged = false
                                    }
                                    mRequestRender = false
                                    sGLThreadManager.notifyAll()
                                    break
                                }
                            }

                            // By design, this is the only place in a GLThread thread where we wait().
                            if (LOG_THREADS) {
                                Log.i(
                                    "GLThread", ("waiting tid=" + id
                                            + " mHaveEglContext: " + mHaveEglContext
                                            + " mHaveEglSurface: " + mHaveEglSurface
                                            + " mPaused: " + mPaused
                                            + " mHasSurface: " + mHasSurface
                                            + " mSurfaceIsBad: " + mSurfaceIsBad
                                            + " mWaitingForSurface: " + mWaitingForSurface
                                            + " mWidth: " + mWidth
                                            + " mHeight: " + mHeight
                                            + " mRequestRender: " + mRequestRender
                                            + " mRenderMode: " + mRenderMode)
                                )
                            }
                            sGLThreadManager.wait()
                        }
                    } // end of synchronized(sGLThreadManager)
                    if (event != null) {
                        event!!.run()
                        event = null
                        continue
                    }
                    if (createEglSurface) {
                        if (LOG_SURFACE) {
                            Log.w("GLThread", "egl createSurface")
                        }
                        if (!mEglHelper!!.createSurface()) {
                            synchronized(sGLThreadManager) {
                                mSurfaceIsBad = true
                                sGLThreadManager.notifyAll()
                            }
                            continue
                        }
                        createEglSurface = false
                    }
                    if (createGlInterface) {
                        gl = mEglHelper!!.createGL() as GL10
                        sGLThreadManager.checkGLDriver(gl)
                        createGlInterface = false
                    }
                    if (createEglContext) {
                        if (LOG_RENDERER) {
                            Log.w("GLThread", "onSurfaceCreated")
                        }
                        val view = mGLSurfaceViewWeakRef.get()
                        view?.mRenderer?.onSurfaceCreated(gl, mEglHelper!!.mEglConfig)
                        createEglContext = false
                    }
                    if (sizeChanged) {
                        if (LOG_RENDERER) {
                            Log.w("GLThread", "onSurfaceChanged($w, $h)")
                        }
                        val view = mGLSurfaceViewWeakRef.get()
                        view?.mRenderer?.onSurfaceChanged(gl, w, h)
                        sizeChanged = false
                    }
                    if (LOG_RENDERER_DRAW_FRAME) {
                        Log.w("GLThread", "onDrawFrame tid=$id")
                    }
                    run {
                        val view = mGLSurfaceViewWeakRef.get()
                        view?.mRenderer?.onDrawFrame(gl)
                    }
                    val swapError = mEglHelper!!.swap()
                    when (swapError) {
                        EGL10.EGL_SUCCESS -> {
                        }
                        EGL11.EGL_CONTEXT_LOST -> {
                            if (LOG_SURFACE) {
                                Log.i("GLThread", "egl context lost tid=$id")
                            }
                            lostEglContext = true
                        }
                        else -> {
                            // Other errors typically mean that the current surface is bad,
                            // probably because the SurfaceView surface has been destroyed,
                            // but we haven't been notified yet.
                            // Log the error to help developers understand why rendering stopped.
                            EglHelper.logEglErrorAsWarning("GLThread", "eglSwapBuffers", swapError)
                            synchronized(sGLThreadManager) {
                                mSurfaceIsBad = true
                                sGLThreadManager.notifyAll()
                            }
                        }
                    }
                    if (wantRenderNotification) {
                        doRenderNotification = true
                    }
                }
            } finally {
                /*
                 * clean-up everything...
                 */
                synchronized(sGLThreadManager) {
                    stopEglSurfaceLocked()
                    stopEglContextLocked()
                }
            }
        }

        fun ableToDraw(): Boolean {
            return mHaveEglContext && mHaveEglSurface && readyToDraw()
        }

        private fun readyToDraw(): Boolean {
            return (!mPaused && mHasSurface && !mSurfaceIsBad
                    && mWidth > 0 && mHeight > 0
                    && (mRequestRender || mRenderMode == RENDERMODE_CONTINUOUSLY))
        }

        var renderMode: Int
            get() {
                synchronized(sGLThreadManager) { return mRenderMode }
            }
            set(renderMode) {
                require((RENDERMODE_WHEN_DIRTY <= renderMode && renderMode <= RENDERMODE_CONTINUOUSLY)) { "renderMode" }
                synchronized(sGLThreadManager) {
                    mRenderMode = renderMode
                    sGLThreadManager.notifyAll()
                }
            }

        fun requestRender() {
            synchronized(sGLThreadManager) {
                mRequestRender = true
                sGLThreadManager.notifyAll()
            }
        }

        fun surfaceCreated() {
            synchronized(sGLThreadManager) {
                if (LOG_THREADS) {
                    Log.i("GLThread", "surfaceCreated tid=" + id)
                }
                mHasSurface = true
                sGLThreadManager.notifyAll()
                while ((mWaitingForSurface) && (!mExited)) {
                    try {
                        sGLThreadManager.wait()
                    } catch (e: InterruptedException) {
                        currentThread().interrupt()
                    }
                }
            }
        }

        fun surfaceDestroyed() {
            synchronized(sGLThreadManager) {
                if (LOG_THREADS) {
                    Log.i("GLThread", "surfaceDestroyed tid=" + id)
                }
                mHasSurface = false
                sGLThreadManager.notifyAll()
                while ((!mWaitingForSurface) && (!mExited)) {
                    try {
                        sGLThreadManager.wait()
                    } catch (e: InterruptedException) {
                        currentThread().interrupt()
                    }
                }
            }
        }

        fun onPause() {
            synchronized(sGLThreadManager) {
                if (LOG_PAUSE_RESUME) {
                    Log.i("GLThread", "onPause tid=" + id)
                }
                mRequestPaused = true
                sGLThreadManager.notifyAll()
                while ((!mExited) && (!mPaused)) {
                    if (LOG_PAUSE_RESUME) {
                        Log.i("Main thread", "onPause waiting for mPaused.")
                    }
                    try {
                        sGLThreadManager.wait()
                    } catch (ex: InterruptedException) {
                        currentThread().interrupt()
                    }
                }
            }
        }

        fun onResume() {
            synchronized(sGLThreadManager) {
                if (LOG_PAUSE_RESUME) {
                    Log.i("GLThread", "onResume tid=" + id)
                }
                mRequestPaused = false
                mRequestRender = true
                mRenderComplete = false
                sGLThreadManager.notifyAll()
                while ((!mExited) && mPaused && (!mRenderComplete)) {
                    if (LOG_PAUSE_RESUME) {
                        Log.i("Main thread", "onResume waiting for !mPaused.")
                    }
                    try {
                        sGLThreadManager.wait()
                    } catch (ex: InterruptedException) {
                        currentThread().interrupt()
                    }
                }
            }
        }

        fun onWindowResize(w: Int, h: Int) {
            synchronized(sGLThreadManager) {
                mWidth = w
                mHeight = h
                mSizeChanged = true
                mRequestRender = true
                mRenderComplete = false
                sGLThreadManager.notifyAll()

                // Wait for thread to react to resize and render a frame
                while ((!mExited && !mPaused && !mRenderComplete
                            && ableToDraw())
                ) {
                    if (LOG_SURFACE) {
                        Log.i(
                            "Main thread",
                            "onWindowResize waiting for render complete from tid=" + id
                        )
                    }
                    try {
                        sGLThreadManager.wait()
                    } catch (ex: InterruptedException) {
                        currentThread().interrupt()
                    }
                }
            }
        }

        fun requestExitAndWait() {
            // don't call this from GLThread thread or it is a guaranteed
            // deadlock!
            synchronized(sGLThreadManager) {
                mShouldExit = true
                sGLThreadManager.notifyAll()
                while (!mExited) {
                    try {
                        sGLThreadManager.wait()
                    } catch (ex: InterruptedException) {
                        currentThread().interrupt()
                    }
                }
            }
        }

        fun requestReleaseEglContextLocked() {
            mShouldReleaseEglContext = true
            sGLThreadManager.notifyAll()
        }

        /**
         * Queue an "event" to be run on the GL rendering thread.
         * @param r the runnable to be run on the GL rendering thread.
         */
        fun queueEvent(r: Runnable?) {
            requireNotNull(r) { "r must not be null" }
            synchronized(sGLThreadManager) {
                mEventQueue.add(r)
                sGLThreadManager.notifyAll()
            }
        }

        // Once the thread is started, all accesses to the following member
        // variables are protected by the sGLThreadManager monitor
        var mShouldExit = false
        var mExited = false
        var mRequestPaused = false
        var mPaused = false
        var mHasSurface = false
        var mSurfaceIsBad = false
        var mWaitingForSurface = false
        var mHaveEglContext = false
        var mHaveEglSurface = false
        var mShouldReleaseEglContext = false
        var mWidth = 0
        var mHeight = 0
        var mRenderMode: Int
        var mRequestRender = true
        var mRenderComplete = false
        var mEventQueue = ArrayList<Runnable>()
        var mSizeChanged = true

        // End of member variables protected by the sGLThreadManager monitor.
        private var mEglHelper: EglHelper? = null

        /**
         * Set once at thread construction time, nulled out when the parent view is garbage
         * called. This weak reference allows the MGLTextureView to be garbage collected while
         * the GLThread is still alive.
         */
        private val mGLSurfaceViewWeakRef: WeakReference<MGLTextureView?>

        init {
            mRenderMode = RENDERMODE_CONTINUOUSLY
            mGLSurfaceViewWeakRef = glSurfaceViewWeakRef
        }
    }

    internal class LogWriter : Writer() {
        override fun close() {
            flushBuilder()
        }

        override fun flush() {
            flushBuilder()
        }

        override fun write(buf: CharArray, offset: Int, count: Int) {
            for (i in 0 until count) {
                val c = buf[offset + i]
                if (c == '\n') {
                    flushBuilder()
                } else {
                    mBuilder.append(c)
                }
            }
        }

        private fun flushBuilder() {
            if (mBuilder.length > 0) {
                Log.v("GLTextureView", mBuilder.toString())
                mBuilder.delete(0, mBuilder.length)
            }
        }

        private val mBuilder = StringBuilder()
    }

    private fun checkRenderThreadState() {
        check(mGLThread == null) { "setRenderer has already been called for this instance." }
    }

    private class GLThreadManager: Object() {
        @Synchronized
        fun threadExiting(thread: GLThread) {
            if (LOG_THREADS) {
                Log.i("GLThread", "exiting tid=" + thread.id)
            }
            thread.mExited = true
            if (mEglOwner === thread) {
                mEglOwner = null
            }
            notifyAll()
        }

        /*
         * Tries once to acquire the right to use an EGL
         * context. Does not block. Requires that we are already
         * in the sGLThreadManager monitor when this is called.
         *
         * @return true if the right to use an EGL context was acquired.
         */
        fun tryAcquireEglContextLocked(thread: GLThread): Boolean {
            if (mEglOwner === thread || mEglOwner == null) {
                mEglOwner = thread
                notifyAll()
                return true
            }
            checkGLESVersion()
            if (mMultipleGLESContextsAllowed) {
                return true
            }
            // Notify the owning thread that it should release the context.
            // TODO: implement a fairness policy. Currently
            // if the owning thread is drawing continuously it will just
            // reacquire the EGL context.
            if (mEglOwner != null) {
                mEglOwner!!.requestReleaseEglContextLocked()
            }
            return false
        }

        /*
         * Releases the EGL context. Requires that we are already in the
         * sGLThreadManager monitor when this is called.
         */
        fun releaseEglContextLocked(thread: GLThread) {
            if (mEglOwner === thread) {
                mEglOwner = null
            }
            notifyAll()
        }

        @Synchronized
        fun shouldReleaseEGLContextWhenPausing(): Boolean {
            // Release the EGL context when pausing even if
            // the hardware supports multiple EGL contexts.
            // Otherwise the device could run out of EGL contexts.
            return mLimitedGLESContexts
        }

        @Synchronized
        fun shouldTerminateEGLWhenPausing(): Boolean {
            checkGLESVersion()
            return !mMultipleGLESContextsAllowed
        }

        @Synchronized
        fun checkGLDriver(gl: GL10) {
            if (!mGLESDriverCheckComplete) {
                checkGLESVersion()
                val renderer = gl.glGetString(GL10.GL_RENDERER)
                if (mGLESVersion < kGLES_20) {
                    mMultipleGLESContextsAllowed = !renderer.startsWith(kMSM7K_RENDERER_PREFIX)
                    notifyAll()
                }
                mLimitedGLESContexts = !mMultipleGLESContextsAllowed
                if (LOG_SURFACE) {
                    Log.w(
                        TAG,
                        "checkGLDriver renderer = \"" + renderer + "\" multipleContextsAllowed = "
                                + mMultipleGLESContextsAllowed
                                + " mLimitedGLESContexts = " + mLimitedGLESContexts
                    )
                }
                mGLESDriverCheckComplete = true
            }
        }

        private fun checkGLESVersion() {
            if (!mGLESVersionCheckComplete) {
//                mGLESVersion = SystemProperties.getInt(
//                        "ro.opengles.version",
//                        ConfigurationInfo.GL_ES_VERSION_UNDEFINED);
//                if (mGLESVersion >= kGLES_20) {
//                    mMultipleGLESContextsAllowed = true;
//                }
//                if (LOG_SURFACE) {
//                    Log.w(TAG, "checkGLESVersion mGLESVersion =" +
//                            " " + mGLESVersion + " mMultipleGLESContextsAllowed = " + mMultipleGLESContextsAllowed);
//                }
                mGLESVersionCheckComplete = true
            }
        }

        /**
         * This check was required for some pre-Android-3.0 hardware. Android 3.0 provides
         * support for hardware-accelerated views, therefore multiple EGL contexts are
         * supported on all Android 3.0+ EGL drivers.
         */
        private var mGLESVersionCheckComplete = false
        private val mGLESVersion = 0
        private var mGLESDriverCheckComplete = false
        private var mMultipleGLESContextsAllowed = false
        private var mLimitedGLESContexts = false
        private var mEglOwner: GLThread? = null

        companion object {
            private const val TAG = "GLThreadManager"
            private const val kGLES_20 = 0x20000
            private const val kMSM7K_RENDERER_PREFIX = "Q3Dimension MSM7500 "
        }
    }

    private val mThisWeakRef = WeakReference<MGLTextureView?>(this)
    private var mGLThread: GLThread? = null
    private var mRenderer: MGLTextureView.Renderer? = null
    private var mDetached = false
    private var mEGLConfigChooser: MGLTextureView.EGLConfigChooser? = null
    private var mEGLContextFactory: MGLTextureView.EGLContextFactory? = null
    private var mEGLWindowSurfaceFactory: MGLTextureView.EGLWindowSurfaceFactory? = null
    private var mGLWrapper: MGLTextureView.GLWrapper? = null
    /**
     * Get the current value of the debug flags.
     * @return the current value of the debug flags.
     */
    /**
     * Set the debug flags to a new value. The value is
     * constructed by OR-together zero or more
     * of the DEBUG_CHECK_* constants. The debug flags take effect
     * whenever a surface is created. The default value is zero.
     * @param debugFlags the new debug flags
     * @see .DEBUG_CHECK_GL_ERROR
     *
     * @see .DEBUG_LOG_GL_CALLS
     */
    var debugFlags = 0
    private var mEGLContextClientVersion = 0
    /**
     * @return true if the EGL context will be preserved when paused
     */
    /**
     * Control whether the EGL context is preserved when the MGLTextureView is paused and
     * resumed.
     *
     *
     * If set to true, then the EGL context may be preserved when the MGLTextureView is paused.
     * Whether the EGL context is actually preserved or not depends upon whether the
     * Android device that the program is running on can support an arbitrary number of EGL
     * contexts or not. Devices that can only support a limited number of EGL contexts must
     * release the  EGL context in order to allow multiple applications to share the GPU.
     *
     *
     * If set to false, the EGL context will be released when the MGLTextureView is paused,
     * and recreated when the MGLTextureView is resumed.
     *
     *
     *
     * The default is false.
     *
     * @param preserveOnPause preserve the EGL context when paused
     */
    var preserveEGLContextOnPause = false

    companion object {
        private const val TAG = "GLTextureView"
        private const val LOG_ATTACH_DETACH = true
        private const val LOG_THREADS = true
        private const val LOG_PAUSE_RESUME = true
        private const val LOG_SURFACE = true
        private const val LOG_RENDERER = true
        private const val LOG_RENDERER_DRAW_FRAME = false
        private const val LOG_EGL = true

        /**
         * The renderer only renders
         * when the surface is created, or when [.requestRender] is called.
         *
         * @see .getRenderMode
         * @see .setRenderMode
         * @see .requestRender
         */
        const val RENDERMODE_WHEN_DIRTY = 0

        /**
         * The renderer is called
         * continuously to re-render the scene.
         *
         * @see .getRenderMode
         * @see .setRenderMode
         */
        const val RENDERMODE_CONTINUOUSLY = 1

        /**
         * Check glError() after every GL call and throw an exception if glError indicates
         * that an error has occurred. This can be used to help track down which OpenGL ES call
         * is causing an error.
         *
         * @see .getDebugFlags
         *
         * @see .setDebugFlags
         */
        const val DEBUG_CHECK_GL_ERROR = 1

        /**
         * Log GL calls to the system log at "verbose" level with tag "GLTextureView".
         *
         * @see .getDebugFlags
         *
         * @see .setDebugFlags
         */
        const val DEBUG_LOG_GL_CALLS = 2
        private val sGLThreadManager = GLThreadManager()
    }
}