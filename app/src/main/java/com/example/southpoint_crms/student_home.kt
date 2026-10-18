import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.southpoint_crms.R

class StudentHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_home)

        // TODO: In a real app, get the logged-in student's name from Firebase Auth
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        tvGreeting.text = "Hello, Student"

        val cvRequestCleaning = findViewById<CardView>(R.id.cvRequestCleaning)
        val cvMyBookings = findViewById<CardView>(R.id.cvMyBookings)
        val cvProfile = findViewById<CardView>(R.id.cvProfile)
        val ivNotifications = findViewById<ImageView>(R.id.ivNotifications)

        // Navigate to Request Cleaning
        cvRequestCleaning.setOnClickListener {
            startActivity(Intent(this, RequestCleaningActivity::class.java))
        }

        // Navigate to My Bookings (We will build this next)
        cvMyBookings.setOnClickListener {
            // startActivity(Intent(this, MyBookingsActivity::class.java))
        }

        // Navigate to Profile
        cvProfile.setOnClickListener {
            // startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Navigate to Notifications
        ivNotifications.setOnClickListener {
            // startActivity(Intent(this, NotificationsActivity::class.java))
        }
    }
}