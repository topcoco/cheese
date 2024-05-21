package coco.cheese.core.api

import coco.cheese.core.Env
import coco.cheese.core.aidl.client.IKeysClient
import coco.cheese.core.engine.javet.Promise
import coco.cheese.core.interfaces.IBase
import coco.cheese.core.interfaces.IEngineBase
import coco.cheese.core.utils.PluginsUtils
import com.caoccao.javet.annotations.V8Function
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.values.reference.V8ValuePromise
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService

class PluginsApi(private val env: Env) : IEngineBase {
    override val executorService: ExecutorService = env.executorService
    override lateinit var nodeRuntime: NodeRuntime
    @V8Function
    override fun setNodeRuntime(key: String) {
        this.nodeRuntime = env.nodeRuntime[key]!!.nodeRuntime
    }


    @V8Function
    fun installAsync(path:String): V8ValuePromise {
        val v8ValuePromiseResolver = nodeRuntime.createV8ValuePromise()
        val task: Promise.Task =
            Promise.Task(v8ValuePromiseResolver, "", System.currentTimeMillis())
        executorService.submit {
            Promise(nodeRuntime, task,
                env.invoke<PluginsUtils>().install(path)
            )
        }
        return v8ValuePromiseResolver.promise
    }
    @V8Function
    fun install(path:String):Boolean {
        return env.invoke<PluginsUtils>().install(path)
    }


    @V8Function
    fun startAsync(): V8ValuePromise {
        val v8ValuePromiseResolver = nodeRuntime.createV8ValuePromise()
        val task: Promise.Task =
            Promise.Task(v8ValuePromiseResolver, "", System.currentTimeMillis())
        executorService.submit {
            Promise(nodeRuntime, task,
                env.invoke<PluginsUtils>().start()
            )
        }
        return v8ValuePromiseResolver.promise
    }
    @V8Function
    fun start() {
         env.invoke<PluginsUtils>().start()
    }

    @V8Function
    fun uninstallAsync(pkg:String): V8ValuePromise {
        val v8ValuePromiseResolver = nodeRuntime.createV8ValuePromise()
        val task: Promise.Task =
            Promise.Task(v8ValuePromiseResolver, "", System.currentTimeMillis())
        executorService.submit {
            Promise(nodeRuntime, task,
                env.invoke<PluginsUtils>().uninstall(pkg)
            )
        }
        return v8ValuePromiseResolver.promise
    }
    @V8Function
    fun uninstall(pkg:String):Boolean {
        return env.invoke<PluginsUtils>().uninstall(pkg)
    }




    companion object : IBase {
        private var instanceWeak: WeakReference<PluginsApi>? = null
        private var instance: PluginsApi? = null
        private val lock = Any()
        override fun get(env: Env, examine: Boolean) : PluginsApi {
            if (this.instance == null || !examine) {
                synchronized(this.lock) {
                    this.instance = PluginsApi(env)
                }
            }
            return this.instance!!
        }

        override fun getWeak(env: Env, examine: Boolean): PluginsApi {
            if (this.instanceWeak?.get() == null || !examine) {
                synchronized(this.lock) {
                    this.instanceWeak = WeakReference(PluginsApi(env))
                }
            }
            return this.instanceWeak?.get()!!
        }

    }
}