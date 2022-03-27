package com.miniclip.robin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.miniclip.robin.databinding.MainActivityBinding
import com.miniclip.robin.ui.GroupStageFragment
import com.miniclip.robin.util.extensions.viewBinding

class MainActivity : AppCompatActivity() {

    private val views by viewBinding<MainActivityBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(views.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, GroupStageFragment())
                .commitNow()
        }
    }

}