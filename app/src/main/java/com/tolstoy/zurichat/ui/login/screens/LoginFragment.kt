package com.tolstoy.zurichat.ui.login.screens

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.tolstoy.zurichat.R
import com.tolstoy.zurichat.data.localSource.Cache
import com.tolstoy.zurichat.databinding.FragmentLoginBinding
import com.tolstoy.zurichat.models.LoginBody
import com.tolstoy.zurichat.models.LoginResponse
import com.tolstoy.zurichat.ui.activities.CreateOrganizationActivity
import com.tolstoy.zurichat.ui.activities.MainActivity
import com.tolstoy.zurichat.ui.login.LoginViewModel
import com.tolstoy.zurichat.ui.organizations.utils.ZuriSharePreference
import com.tolstoy.zurichat.util.Result
import com.tolstoy.zurichat.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val binding by viewBinding(FragmentLoginBinding::bind)
    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var prevDest: String

    private lateinit var progressDialog: ProgressDialog
    @Inject
    lateinit var sharedPreferences: SharedPreferences


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textView = binding.textViewRegister
        val materialTextView = binding.materialTextView
        progressDialog = ProgressDialog(context)

        prevDest = Navigation.findNavController(view).previousBackStackEntry!!
            .destination.label.toString()

        if (prevDest == "fragment_email_verified"){
            binding.email.setText(arguments?.getString("email"))
        }


        textView.setOnClickListener(fun(it: View) {
            findNavController().navigate(R.id.action_loginFragment_to_registerUserFragment)
        })

        materialTextView.setOnClickListener(fun(it: View) {
            findNavController().navigate(R.id.forgotPasswordFragment)
        })


        handleSignIn()
        setupObservers()
    }

    private fun handleSignIn() = with(binding) {
        buttonSignIn.setOnClickListener {
            val loginBody = LoginBody(
                email = email.text.toString().trim(),
              
                password = password.text.toString(),

            )
            viewModel.login(loginBody)
        }
    }

    private fun setupObservers() {
        viewModel.loginResponse.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> handleLoading()
                is Result.Success -> handleSuccess(result.data)
                is Result.Error -> handleError(result.error)
            }
        }
    }

    private fun handleLoading() {
        Toast.makeText(context, "Please wait", Toast.LENGTH_LONG).show()
        Timber.d("Loading...")
        progressDialog.show()
    }

    private fun handleSuccess(response: LoginResponse) {
        //val action = LoginFragmentDirections.actionLoginFragmentToMainNav(response.data.user)
        // findNavController().navigate(action)
        //findNavController().navigate(R.id.action_loginFragment_to_main_nav,bundle)

        //Starting A Activity With A Navigation Component Causes Issues With The Activity Theme.
        //Better To Sse An Intent

        // add user auth state to shared preference
        viewModel.saveUserAuthState(true)

        val user = response.data.user.copy(currentUser = true)

        // add user object to room database
        viewModel.saveUser(user)
        progressDialog.dismiss()
        val bundle = Bundle()
        bundle.putParcelable("USER", user)

        /*
            if the user is just signed up and logged in and redirect him to an activity
            where he will create new organization.
            if the user is not logging in for the first time redirect him to home activity
        */
        if(prevDest == "fragment_email_verified"){
            val intent = Intent(requireContext(), CreateOrganizationActivity::class.java)
            Cache.map.putIfAbsent("user", user)
            intent.putExtras(bundle)
            startActivity(intent)
            requireActivity().finish()
        }else{
            val intent = Intent(requireContext(), MainActivity::class.java)
            Cache.map.putIfAbsent("user", user)
            intent.putExtras(bundle)
            startActivity(intent)
            requireActivity().finish()
        }
        sharedPreferences.edit().putString("TOKEN",user.token).apply()
        Toast.makeText(context, "You have successfully logged in", Toast.LENGTH_LONG).show()
        ZuriSharePreference(requireContext()).setString("TOKEN", user.token)
    }

    private fun handleError(throwable: Throwable) {
        Toast.makeText(context, "Invalid email or password, please sign up", Toast.LENGTH_LONG)
            .show()
        Timber.e(throwable)
        progressDialog.dismiss()
    }

}

