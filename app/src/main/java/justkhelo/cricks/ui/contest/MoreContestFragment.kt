package justkhelo.cricks.ui.contest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.edify.atrist.listener.OnContestEvents
import com.edify.atrist.listener.OnContestLoadedListener
import justkhelo.cricks.ContestActivity
import justkhelo.cricks.LeadersBoardActivity
import justkhelo.cricks.R
import justkhelo.cricks.databinding.FragmentMoreContestBinding
import justkhelo.cricks.models.UpcomingMatchesModel
import justkhelo.cricks.ui.contest.adaptors.ContestListAdapter
import justkhelo.cricks.models.ContestModelLists
import justkhelo.cricks.utils.CustomeProgressDialog

class MoreContestFragment : Fragment() {
    var mListenerContestEvents: OnContestLoadedListener? = null
    var mListener: OnContestLoadedListener? = null

    private lateinit var allContestList: java.util.ArrayList<ContestModelLists>
    var objectMatches: UpcomingMatchesModel? = null
    private lateinit var customeProgressDialog: CustomeProgressDialog
    private var mBinding: FragmentMoreContestBinding? = null
    lateinit var adapter: ContestListAdapter

    companion object {
        const val CONTEST_LIST: String = "contestlist"

        fun newInstance(bundle: Bundle): MoreContestFragment {
            val fragment = MoreContestFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnContestLoadedListener) {
            mListener = context
        } else {
            throw RuntimeException(
                "$context must implement OnContestLoadedListener"
            )
        }

        if (context is OnContestEvents) {
            mListenerContestEvents = context
        } else {
            throw RuntimeException(
                "$context must implement OnContestLoadedListener"
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_more_contest, container, false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customeProgressDialog = CustomeProgressDialog(activity)
    }

    override fun onResume() {
        super.onResume()
        objectMatches =
            requireArguments().get(ContestActivity.SERIALIZABLE_KEY_MATCH_OBJECT) as UpcomingMatchesModel
        allContestList = requireArguments().get(CONTEST_LIST) as ArrayList<ContestModelLists>

        mBinding!!.recyclerMyContest.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val colorCode = requireActivity().resources.getColor(R.color.white)
        adapter = ContestListAdapter(
            requireActivity(),
            allContestList,
            objectMatches!!,
            mListenerContestEvents as OnContestEvents,
            colorCode
        )
        mBinding!!.recyclerMyContest.adapter = adapter

        adapter.onItemClick = { objects ->
            val intent = Intent(context, LeadersBoardActivity::class.java)
            intent.putExtra(LeadersBoardActivity.SERIALIZABLE_MATCH_KEY, objectMatches)
            intent.putExtra(LeadersBoardActivity.SERIALIZABLE_CONTEST_KEY, objects)
            requireActivity().startActivityForResult(
                intent,
                LeadersBoardActivity.CREATETEAM_REQUESTCODE
            )
        }
    }
}
