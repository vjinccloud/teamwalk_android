package com.taiwanlife.teamwalk.ui.onboarding

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseAdapter
import com.taiwanlife.teamwalk.databinding.ActivityAvatarBinding
import com.taiwanlife.teamwalk.databinding.ItemAvatarBinding
import com.taiwanlife.teamwalk.ui.onboarding.AvatarActivity.OptionType.BLUE
import com.taiwanlife.teamwalk.ui.onboarding.AvatarActivity.OptionType.ORANGE
import com.taiwanlife.teamwalk.ui.onboarding.AvatarActivity.OptionType.PURPLE
import com.taiwanlife.teamwalk.ui.onboarding.AvatarActivity.OptionType.RED
import com.taiwanlife.teamwalk.ui.onboarding.AvatarActivity.OptionType.ROYAL_PURPLE
import com.taiwanlife.teamwalk.ui.onboarding.AvatarActivity.OptionType.YELLOW
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.toast

class AvatarActivity :
    OnBoardingActivity<ActivityAvatarBinding>({ ActivityAvatarBinding.inflate(it) }) {

    private enum class OptionType {
        RED, ORANGE, YELLOW, BLUE, PURPLE, ROYAL_PURPLE
    }

    private var currentOptionType = RED
    private val optionMap = mutableMapOf<OptionType, List<Int>>()
    private lateinit var avatarAdapter: AvatarAdapter

    private var buildInImage = -1
    private var filter = 0

    override fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onLastCreateBaseActivity(view, savedInstanceState)

        viewBinding.onboardingSkipButton.setOnClickListener {
            saveUserAndFinishAll()
        }
        viewBinding.back.setOnClickListener {
            finish()
        }

        setOptions()
        setAvatars()
        setNickName()
        viewBinding.onboardingNextButton.setOnClickListener {
//            val text = userInfo.nickname
//            if (!TextUtils.isEmpty(text)) {
//                val chars: CharArray = text!!.toCharArray()
//                for (aChar in chars) {
//                    val type = Character.getType(aChar)
//                    if (type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt()) {
//                        toast(R.string.onboard_edit_nickname_emoji)
//                        return@setOnClickListener
//                    }
//                }
//            }
            if (buildInImage != -1) {
                userInfo = userInfo.copy(userAvatar = "${buildInImage}.png")
            }

            toConnectActivity()
        }
    }


    private fun setAvatars() {
        avatarAdapter = AvatarAdapter()
        viewBinding.onboardingRecyclerCoverFlowAvatar.adapter = avatarAdapter
        viewBinding.onboardingRecyclerCoverFlowAvatar.setIntervalRatio(0.66f)
        viewBinding.onboardingRecyclerCoverFlowAvatar.setAlphaItem(true)
        val changePosition: (Int) -> Unit = { position ->
            var avatar = position + 1
            filter = 0
            avatar += when (currentOptionType) {
                RED -> 0
                ORANGE -> 6
                YELLOW -> 12
                BLUE -> 18
                PURPLE -> 24
                ROYAL_PURPLE -> 30
            }
            buildInImage = avatar
        }
        viewBinding.onboardingRecyclerCoverFlowAvatar.setOnItemSelectedListener(changePosition)

        val resetAdapter = {
            when (currentOptionType) {
                RED -> avatarAdapter.setModels(optionMap[RED]!!.toMutableList())
                ORANGE -> avatarAdapter.setModels(optionMap[ORANGE]!!.toMutableList())
                YELLOW -> avatarAdapter.setModels(optionMap[YELLOW]!!.toMutableList())
                BLUE -> avatarAdapter.setModels(optionMap[BLUE]!!.toMutableList())
                PURPLE -> avatarAdapter.setModels(optionMap[PURPLE]!!.toMutableList())
                ROYAL_PURPLE -> avatarAdapter.setModels(optionMap[ROYAL_PURPLE]!!.toMutableList())
            }
            changePosition(viewBinding.onboardingRecyclerCoverFlowAvatar.coverFlowLayout.selectedPos)
//            viewBinding.onboardingRecyclerCoverFlowAvatar.coverFlowLayout.scrollToPosition(1)
        }

        viewBinding.onboardingImageButtonRed.setOnClickListener {
            currentOptionType = RED
            resetAdapter()
        }
        viewBinding.onboardingImageButtonOrange.setOnClickListener {
            currentOptionType = ORANGE
            resetAdapter()
        }
        viewBinding.onboardingImageButtonYellow.setOnClickListener {
            currentOptionType = YELLOW
            resetAdapter()
        }
        viewBinding.onboardingImageButtonBlue.setOnClickListener {
            currentOptionType = BLUE
            resetAdapter()
        }
        viewBinding.onboardingImageButtonPurple.setOnClickListener {
            currentOptionType = PURPLE
            resetAdapter()
        }
        viewBinding.onboardingImageButtonRoyalpurple.setOnClickListener {
            currentOptionType = ROYAL_PURPLE
            resetAdapter()
        }

        // 手動選紅色
        viewBinding.onboardingImageButtonRed.performClick()
        viewBinding.onboardingRecyclerCoverFlowAvatar.coverFlowLayout.scrollToPosition(1)
    }

    private fun setNickName() {
        viewBinding.onboardingEditTextNickname.setText(userInfo.nickname)
        viewBinding.onboardingEditTextNickname.addTextChangedListener(object : TextWatcher {
            private val maxLength = 8
            private var currentEnd = 0

            override fun beforeTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                currentEnd = start + count
            }

            override fun afterTextChanged(s: Editable) {
                while (Utils.calculateChineseLength(s) > maxLength) {
                    currentEnd--
                    s.delete(currentEnd, currentEnd + 1)
                }
                val text = s.toString().trim { it <= ' ' }
                validate(text)
            }
        })
    }

    private fun validate(nickname: String) {
        if (TextUtils.isEmpty(nickname)) {
            toast(getString(R.string.onboard_edit_nickname) + " " + getString(R.string.empty))
            userInfo = userInfo.copy(nickname = "")
        } else {
            userInfo = userInfo.copy(nickname = nickname)
        }
    }

    private class AvatarAdapter() : BaseAdapter<Int, ItemAvatarBinding>() {
        override fun getViewBinding(
            layoutInflater: LayoutInflater,
            parent: ViewGroup
        ): ItemAvatarBinding {
            return ItemAvatarBinding.inflate(layoutInflater, parent, false)
        }

        override fun onBindViewBinding(
            viewBinding: ItemAvatarBinding,
            position: Int
        ) {
            viewBinding.onboardingImageAvatar.setImageResource(getModels()[position])
        }

    }

    private fun setOptions() {
        optionMap.put(
            OptionType.RED,
            listOf(
                R.drawable.avatar_1_red,
                R.drawable.avatar_2_red,
                R.drawable.avatar_3_red,
                R.drawable.avatar_4_red,
                R.drawable.avatar_5_red,
                R.drawable.avatar_6_red
            )
        )
        optionMap.put(
            OptionType.ORANGE,
            listOf(
                R.drawable.avatar_1_orange,
                R.drawable.avatar_2_orange,
                R.drawable.avatar_3_orange,
                R.drawable.avatar_4_orange,
                R.drawable.avatar_5_orange,
                R.drawable.avatar_6_orange
            )
        )
        optionMap.put(
            OptionType.YELLOW,
            listOf(
                R.drawable.avatar_1_yellow,
                R.drawable.avatar_2_yellow,
                R.drawable.avatar_3_yellow,
                R.drawable.avatar_4_yellow,
                R.drawable.avatar_5_yellow,
                R.drawable.avatar_6_yellow
            )
        )
        optionMap.put(
            OptionType.BLUE,
            listOf(
                R.drawable.avatar_1_blue,
                R.drawable.avatar_2_blue,
                R.drawable.avatar_3_blue,
                R.drawable.avatar_4_blue,
                R.drawable.avatar_5_blue,
                R.drawable.avatar_6_blue
            )
        )
        optionMap.put(
            OptionType.PURPLE,
            listOf(
                R.drawable.avatar_1_purple,
                R.drawable.avatar_2_purple,
                R.drawable.avatar_3_purple,
                R.drawable.avatar_4_purple,
                R.drawable.avatar_5_purple,
                R.drawable.avatar_6_purple
            )
        )
        optionMap.put(
            OptionType.ROYAL_PURPLE,
            listOf(
                R.drawable.avatar_1_royalpurple,
                R.drawable.avatar_2_royalpurple,
                R.drawable.avatar_3_royalpurple,
                R.drawable.avatar_4_royalpurple,
                R.drawable.avatar_5_royalpurple,
                R.drawable.avatar_6_royalpurple
            )
        )
    }
}