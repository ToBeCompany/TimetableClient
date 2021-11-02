package com.example.timetable

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.timetable.data.BusData
import com.example.timetable.data.BusStop
import com.example.timetable.firebase.BusFireBase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class MainActivity : AppCompatActivity()
{

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

/*        var db = BusFireBase()
        db.setBus(
            BusData(
                name = "1",
                mutableListOf<GeoPoint>(
                    GeoPoint(53.364922, 83.676367),
                    GeoPoint(53.374683, 83.67881),
                    GeoPoint(53.380654,83.683648),
                    GeoPoint(53.380422, 83.693066),
                    GeoPoint(53.379277, 83.697074),
                    GeoPoint(53.379216, 83.713202),
                    GeoPoint(53.373675, 83.713332),
                    GeoPoint(53.357037, 83.698841)
                ),
                mutableListOf<BusStop>(
                    BusStop("Поликлинника №8", "6:30", GeoPoint(53.36499065277956, 83.67648890343735)),
                    BusStop("ул. Юрина (рынок Докучаево)", "6:33", GeoPoint(53.370042603040986, 83.6777242364155) ),
                    BusStop("ул. Гущина (Детс. краевая больница)", "6:35", GeoPoint(53.376438953190835, 83.68050058674137 ) ),
                    BusStop("ул. Озерная", "6:38", GeoPoint(53.3800624793975, 83.68343646758834) ),
                    BusStop("ул. Э. Алексеевой (Барн. водяная компания)", "6:39", GeoPoint(53.37919404771489, 83.7058633794418) ),
                    BusStop("ул. Малахова", "6:40", GeoPoint(53.378818102382944, 83.71267659416681) ),
                    BusStop("ост. Бия", "6:41", GeoPoint(53.37421645384774, 83.71269014929246) ),
                    BusStop("ост. Аптека", "6:43", GeoPoint(53.36964150618117, 83.70885856959022) ),
                    BusStop("ул. Г. Исакова", "6.44", GeoPoint(53.363260234674584, 83.70324045515467) ),
                    BusStop("ТРЦ Огни", "6.46", GeoPoint(53.35796146027083, 83.69918583011008) ),
                    BusStop("Рынок Янтарный", "6:48", GeoPoint(53.350608, 83.695658) ),
                    BusStop("ул. 50 лет СССР", "6:49", GeoPoint(53.3462661445962, 83.69594851753867) ),
                    BusStop("станция Ползуново", "7:05", GeoPoint(53.299012139382796, 83.70273236968268) ),
                )
            )
        )*/
    }
}
