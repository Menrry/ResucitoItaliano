package it.cammino.risuscito.ui

import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.multidex.MultiDexApplication
import android.widget.ImageView

import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.squareup.picasso.Picasso

import it.cammino.risuscito.R
import it.cammino.risuscito.database.RisuscitoDatabase
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

@Suppress("unused")
class RisuscitoApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        RisuscitoDatabase.getInstance(this)

        CalligraphyConfig.initDefault(
                CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build())

        // initialize and create the image loader logic
        DrawerImageLoader.init(
                object : AbstractDrawerImageLoader() {
                    override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String?) {
//                        Picasso.with(imageView.context).load(uri).placeholder(placeholder).into(imageView)
                        Picasso.get().load(uri).placeholder(placeholder).into(imageView)
                    }

                    override fun cancel(imageView: ImageView?) {
//                        Picasso.with(imageView!!.context).cancelRequest(imageView)
                        Picasso.get().cancelRequest(imageView!!)
                    }
                })
    }
}