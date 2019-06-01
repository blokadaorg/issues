package filter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import com.github.salomonbrys.kodein.instance
import core.*
import gs.environment.ComponentProvider
import gs.environment.inject
import gs.property.Device
import org.blokada.R

class FiltersExportView(
        ctx: Context,
        attributeSet: AttributeSet
) : ScrollView(ctx, attributeSet) {

    var text = ""
        get() { return field.trim() }
        set(value) {
            field = value
            buttonView.text = if (value.isEmpty()) context.getString(R.string.filter_export_select)
            else value
            updateError()
        }

    var showError = false
        set(value) {
            field = value
            updateError()
        }

    var correct = false
        set(value) {
            field = value
            updateError()
        }

    fun reset() {
        text = ""
        showError = false
        correct = false
        import.isEnabled = true
    }

    private val buttonView by lazy { findViewById<Button>(R.id.button1) }
    private val button2View by lazy { findViewById<Button>(R.id.button2) }
    private val errorView by lazy { findViewById<ViewGroup>(R.id.filter_error) }
    private val import by lazy { findViewById<Button>(R.id.filter_import_checkbox) }
    private val importGroup by lazy { findViewById<ViewGroup>(R.id.filter_comment_group) }

    private val activity by lazy { ctx.inject().instance<ComponentProvider<MainActivity>>() }
    private val t by lazy { ctx.inject().instance<tunnel.Main>() }
    private val d by lazy { ctx.inject().instance<Device>() }

    var uri: android.net.Uri? = null
        set(value) {
            field = value
            if (value == null) text = ""
            text = value.toString()
        }

    var flags: Int = 0

    override fun onFinishInflate() {
        super.onFinishInflate()
        updateError()
        importGroup.visibility = View.GONE

        buttonView.setOnClickListener {
            buttonView.isEnabled = false
            button2View.isEnabled = true
            buttonView.alpha = 0.5f
            button2View.alpha = 1f
            importGroup.visibility = View.GONE
            context.ktx().v("resetting persistence path")
            Persistence.global.savePath(Persistence.DEFAULT_PATH)
        }

        button2View.setOnClickListener {
            button2View.isEnabled = false
            buttonView.isEnabled = true
            button2View.alpha = 0.5f
            buttonView.alpha = 1f
            importGroup.visibility = View.VISIBLE
            context.ktx().v("set persistence path", getExternalPath())
            Persistence.global.savePath(getExternalPath())

            if (!checkStoragePermissions(context.ktx())) {
                activity.get()?.apply {
                    askStoragePermission(context.ktx(), this)
                }
            }
        }

        if (Persistence.global.loadPath() == Persistence.DEFAULT_PATH) {
            buttonView.alpha = 0.5f
            button2View.alpha = 1.0f
            buttonView.isEnabled = false
            button2View.isEnabled = true
        } else {
            buttonView.alpha = 1.0f
            button2View.alpha = 0.5f
            buttonView.isEnabled = true
            button2View.isEnabled = false
        }

//        buttonView.setOnClickListener {
//            val a = activity.get()
//            if (a != null) {
//                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
//
////                intent.type = "text/plain"
////                intent.addCategory(Intent.CATEGORY_OPENABLE)
//
//                a.addOnNextActivityResultListener { result: Int, data: Intent? ->
//                    if (result != Activity.RESULT_OK || data == null) {
//                        uri = null
//                        correct = false
//                        showError = true
//                        cmd.send(SetFiltersPath(path = null))
//                    } else {
//                        val f = DocumentFile.fromTreeUri(context, data.data)
//                        if (!f.canWrite()) {
//                            uri = null
//                            correct = false
//                            showError = true
//                            cmd.send(SetFiltersPath(path = null))
//                        } else {
//                            uri = f.uri
//                            correct = true
//                            flags = data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION and
//                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//                            context.contentResolver.takePersistableUriPermission(uri, flags)
//                            cmd.send(SetFiltersPath(path = uri.toString()))
//                        }
//                    }
//                }
//
//                a.startActivityForResult(Intent.createChooser(intent,
//                            context.getString(R.string.filter_export_select)), 0)
//            }
//        }

        import.setOnClickListener {
            import.isEnabled = false
            import.alpha = 0.5f
            t.reloadConfig(context.ktx("import:filters"), d.onWifi())
        }
    }

    private fun updateError() {
        if (showError && !correct) {
            errorView.visibility = View.VISIBLE
            import.isEnabled = false
        } else {
            errorView.visibility = View.GONE
            import.isEnabled = true
        }
    }

}
