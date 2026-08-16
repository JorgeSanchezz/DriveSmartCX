package com.drivesmart.cx.util

import androidx.compose.ui.graphics.Color
import com.drivesmart.cx.R

enum class VehicleBrand(val displayName: String, val color: Color, val iconRes: Int, val isMoto: Boolean = false) {
    DODGE("Dodge", Color(0xFFE31837), R.drawable.ic_logo_dodge),
    TOYOTA("Toyota", Color(0xFFEB0A1E), R.drawable.ic_logo_toyota),
    NISSAN("Nissan", Color(0xFFC11030), R.drawable.ic_logo_nissan),
    FORD("Ford", Color(0xFF003478), R.drawable.ic_logo_ford),
    CHEVROLET("Chevrolet", Color(0xFFAD8D54), R.drawable.ic_logo_chevrolet),
    HONDA("Honda", Color(0xFFE40521), R.drawable.ic_logo_honda),
    VOLKSWAGEN("Volkswagen", Color(0xFF001E50), R.drawable.ic_logo_volkswagen),
    BMW("BMW", Color(0xFF0066B3), R.drawable.ic_logo_bmw),
    MERCEDES("Mercedes-Benz", Color(0xFF000000), R.drawable.ic_logo_mercedes),
    
    YAMAHA("Yamaha", Color(0xFF004DAE), R.drawable.ic_logo_yamaha, true),
    KAWASAKI("Kawasaki", Color(0xFF66FF00), R.drawable.ic_logo_kawasaki, true),
    ITALIKA("Italika", Color(0xFFE31837), R.drawable.ic_logo_italika, true),
    HARLEY("Harley-Davidson", Color(0xFFFF6600), R.drawable.ic_logo_harley, true),
    
    OTRO("Otro", Color(0xFF607D8B), android.R.drawable.ic_menu_directions);

    companion object {
        fun fromString(name: String): VehicleBrand {
            return entries.find { it.displayName == name } ?: OTRO
        }
        
        fun getBrandsForType(tipo: String): List<VehicleBrand> {
            return entries.filter { 
                if (it == OTRO) true
                else if (tipo == "Moto") it.isMoto 
                else !it.isMoto 
            }
        }
    }
}
