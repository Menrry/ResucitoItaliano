package it.cammino.risuscito

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.viewmodels.GeneralIndexViewModel
import kotlinx.android.synthetic.main.tabs_layout.*

class GeneralIndex : Fragment() {

    private var mMainActivity: MainActivity? = null

    private lateinit var mViewModel: GeneralIndexViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.tabs_layout, container, false)

        mViewModel = ViewModelProviders.of(this).get(GeneralIndexViewModel::class.java)

        mMainActivity = activity as? MainActivity
        mMainActivity?.setupToolbarTitle(R.string.title_activity_general_index)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view_pager.adapter = SectionsPagerAdapter(childFragmentManager)

        mMainActivity?.setTabVisible(true)
        mMainActivity?.enableFab(false)
        mMainActivity?.enableBottombar(false)
        if (savedInstanceState == null) {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            view_pager.currentItem = Integer.parseInt(pref.getString(Utility.DEFAULT_INDEX, "0")
                    ?: "0")
        } else
            view_pager.currentItem = mViewModel.pageViewed
        mMainActivity?.getMaterialTabs()?.setupWithViewPager(view_pager)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mViewModel.pageViewed = view_pager.currentItem
    }

    private inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> SimpleIndexFragment.newInstance(0)
                1 -> SimpleIndexFragment.newInstance(1)
                2 -> SectionedIndexFragment.newInstance(0)
                3 -> SimpleIndexFragment.newInstance(2)
                4 -> SectionedIndexFragment.newInstance(1)
                else -> SimpleIndexFragment.newInstance(1)
            }
        }

        override fun getCount(): Int {
            return 5
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val l = ThemeableActivity.getSystemLocalWrapper(requireActivity().resources.configuration)
            when (position) {
                0 -> return getString(R.string.letter_order_text).toUpperCase(l)
                1 -> return getString(R.string.page_order_text).toUpperCase(l)
                2 -> return getString(R.string.arg_search_text).toUpperCase(l)
                3 -> return getString(R.string.salmi_musica_index).toUpperCase(l)
                4 -> return getString(R.string.indice_liturgico_index).toUpperCase(l)
            }
            return null
        }
    }

    companion object {
        internal val TAG = GeneralIndex::class.java.canonicalName
    }

}
