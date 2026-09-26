package com.example.solaragrid.ui.dashboard

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.solaragrid.R
import com.example.solaragrid.database.UserManager

class OperatorHomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_operator_home, container, false)

        val tvOpName = view.findViewById<TextView>(R.id.tvOpName)
        val btnAvatar = view.findViewById<TextView>(R.id.btnAvatar)

        //Get the Operator's name from SQLite
        val user = UserManager(requireContext()).getLoggedInUser()
        val fullName = user?.name ?: "Operator"
        
        tvOpName.text = fullName

        //Generate Initials
        val initials = fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2).uppercase()
        btnAvatar.text = initials

        //Click listener for the Avatar to Log Out
        btnAvatar.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    // Clear SQLite and route to Login
                    UserManager(requireContext()).clearUser()
                    val intent = android.content.Intent(requireContext(), com.example.solaragrid.ui.auth.LoginActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        return view
    }
}
