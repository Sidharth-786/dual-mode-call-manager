package com.example.dualmodecallmanager.service

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.dualmodecallmanager.R
import com.example.dualmodecallmanager.data.model.AppMode
import com.example.dualmodecallmanager.data.preferences.PreferencesManager

class ModeQuickSettingsTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        runCatching {
            val prefs = PreferencesManager.getInstance(applicationContext)
            prefs.toggleMode()
            updateTileState()
        }
    }

    private fun updateTileState() {
        runCatching {
            val tile = qsTile ?: return
            val prefs = PreferencesManager.getInstance(applicationContext)
            val activeMode = prefs.activeMode

            when (activeMode) {
                AppMode.WORK -> {
                    tile.label = "Work Mode"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        tile.subtitle = "Work calls allowed"
                    }
                    tile.state = Tile.STATE_ACTIVE
                    tile.icon = Icon.createWithResource(this, R.drawable.ic_work_tile)
                }
                AppMode.PERSONAL -> {
                    tile.label = "Personal Mode"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        tile.subtitle = "Personal calls allowed"
                    }
                    tile.state = Tile.STATE_INACTIVE
                    tile.icon = Icon.createWithResource(this, R.drawable.ic_personal_tile)
                }
            }
            tile.updateTile()
        }
    }
}
