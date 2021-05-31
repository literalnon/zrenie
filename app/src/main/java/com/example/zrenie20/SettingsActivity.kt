package com.example.zrenie20

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.zrenie20.augmentedFace.augmentedfaces.AugmentedFacesActivity
import com.example.zrenie20.augmentedimage.AugmentedImageActivity
import com.example.zrenie20.myarsample.SpaceActivity
import kotlinx.android.synthetic.main.activity_settings.*

enum class SCREENS {
    SPACE,
    AUGMENTED_FACES,
    AUGMENTED_IMAGE,
    LOCATION
}

class SettingsActivity : AppCompatActivity() {

    companion object {
        private var currentScreen: SCREENS = SCREENS.AUGMENTED_IMAGE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        llSpace?.setOnClickListener {
            currentScreen = SCREENS.SPACE
            startActivity(Intent(this, SpaceActivity::class.java))
        }

        llBodyPart?.setOnClickListener {
            currentScreen = SCREENS.AUGMENTED_FACES
            startActivity(Intent(this, AugmentedFacesActivity::class.java))
        }

        llAugmentedImage?.setOnClickListener {
            currentScreen = SCREENS.AUGMENTED_IMAGE
            startActivity(Intent(this, AugmentedImageActivity::class.java))
        }

        tvLibAr?.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        ivSpaceSelected.visibility = View.GONE
        tvSpace.setTextColor(Color.WHITE)
        ivSpace.setColorFilter(Color.WHITE)

        ivLocationSelected.visibility = View.GONE
        tvLocation.setTextColor(Color.WHITE)
        ivLocation.setColorFilter(Color.WHITE)

        ivAugmentedImageSelected.visibility = View.GONE
        tvAugmentedImage.setTextColor(Color.WHITE)
        ivAugmentedImage.setColorFilter(Color.WHITE)

        ivBodyPartSelected.visibility = View.GONE
        tvBodyPart.setTextColor(Color.WHITE)
        ivBodyPart.setColorFilter(Color.WHITE)

        ivBack?.setOnClickListener {
            onBackPressed()
        }

        val selectedColor = ContextCompat.getColor(this, R.color.selectedColor)

        when (currentScreen) {
            SCREENS.SPACE -> {
                tvSpace.setTextColor(selectedColor)
                ivSpace.setColorFilter(selectedColor)
                ivSpaceSelected.visibility = View.VISIBLE
            }
            SCREENS.AUGMENTED_FACES -> {
                tvBodyPart.setTextColor(selectedColor)
                ivBodyPart.setColorFilter(selectedColor)
                ivBodyPartSelected.visibility = View.VISIBLE
            }
            SCREENS.AUGMENTED_IMAGE -> {
                tvAugmentedImage.setTextColor(selectedColor)
                ivAugmentedImage.setColorFilter(selectedColor)
                ivAugmentedImageSelected.visibility = View.VISIBLE
            }
            SCREENS.LOCATION -> {
                tvLocation.setTextColor(selectedColor)
                ivLocation.setColorFilter(selectedColor)
                ivLocationSelected.visibility = View.VISIBLE
            }
        }
    }
}