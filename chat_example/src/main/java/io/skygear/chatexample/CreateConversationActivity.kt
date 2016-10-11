package io.skygear.chatexample

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Button
import android.widget.Toast
import io.skygear.plugins.chat.callbacks.GetCallback
import io.skygear.plugins.chat.callbacks.SaveCallback
import io.skygear.plugins.chat.chatUser.ChatUser
import io.skygear.plugins.chat.chatUser.ChatUserContainer
import io.skygear.plugins.chat.conversation.Conversation
import io.skygear.plugins.chat.conversation.ConversationContainer
import io.skygear.skygear.Container

class CreateConversationActivity : AppCompatActivity() {
    private val LOG_TAG: String? = "CreateConversation"

    private var mSkygear: Container? = null
    private var mConversationContainer: ConversationContainer? = null
    private var mChatUserContainer: ChatUserContainer? = null
    private var mAdapter: ChatUsesAdapter? = null
    private var mCreateBtn: Button? = null
    private var mUserIdsRv: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_conversation)

        mSkygear = Container.defaultContainer(this)
        mConversationContainer = ConversationContainer.getInstance(mSkygear)
        mChatUserContainer = ChatUserContainer.getInstance(mSkygear)

        mUserIdsRv = findViewById(R.id.chat_users_rv) as RecyclerView
        mCreateBtn = findViewById(R.id.create_conversation_btn) as Button
        mAdapter = ChatUsesAdapter(mSkygear?.currentUser?.id)
        mUserIdsRv?.adapter = mAdapter
        mUserIdsRv?.layoutManager = LinearLayoutManager(this)

        mCreateBtn?.setOnClickListener {
            val selectedUsers = mAdapter?.getSelected()
            if (selectedUsers?.size != 0) {
                createTitleDialog()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        mChatUserContainer?.getAll(object : GetCallback<List<ChatUser>>{
            override fun onSucc(list: List<ChatUser>?) {
                mAdapter?.setChatUsers(list);
            }

            override fun onFail(failReason: String?) {

            }
        })
    }

    fun createTitleDialog() {
        val f = TitleFragment()
        f.setOnOkBtnClickedListener { t -> createConversation(mAdapter?.getSelected(), t) }
        f.show(supportFragmentManager, "create_conversation")
    }

    fun createConversation(users: List<ChatUser>?, title: String?) {
        if (users != null && users.size > 0) {
            val participantIds: MutableList<String> = mutableListOf()
            for (user in users) {
                participantIds.add(user.id)
            }
            val currentUser = mSkygear?.currentUser
            if (currentUser != null) {
                participantIds.add(currentUser.id)
            }

            val loading = ProgressDialog(this)
            loading.setTitle(R.string.loading)
            loading.setMessage(getString(R.string.creating))
            loading.show()

            mConversationContainer?.create(participantIds, participantIds, title,
                    object : SaveCallback<Conversation> {
                        override fun onSucc(`object`: Conversation?) {
                            loading.dismiss()

                            finish()
                        }

                        override fun onFail(failReason: String?) {
                            loading.dismiss()

                            if (failReason != null) {
                                toast(failReason)
                            }
                        }
                    })
        }
    }

    fun toast(r: String) {
        val context = applicationContext
        val duration = Toast.LENGTH_SHORT

        val toast = Toast.makeText(context, r, duration)
        toast.show()
    }
}
