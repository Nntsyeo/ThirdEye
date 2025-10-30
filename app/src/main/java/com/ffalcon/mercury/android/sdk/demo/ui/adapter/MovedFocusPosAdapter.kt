package com.ffalcon.mercury.android.sdk.demo.ui.adapter

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.NonNull
import com.ffalcon.mercury.android.sdk.core.make3DEffectForSide
import com.ffalcon.mercury.android.sdk.demo.R
import com.ffalcon.mercury.android.sdk.demo.databinding.ItemTelephoneFavoriteMovedBinding
import com.ffalcon.mercury.android.sdk.demo.ui.entity.Contact
import com.ffalcon.mercury.android.sdk.ui.util.RecyclerViewFocusTracker
import com.ffalcon.mercury.android.sdk.ext.BaseBindingHolder
import com.ffalcon.mercury.android.sdk.ext.SimpleBindingAdapter
import com.ffalcon.mercury.android.sdk.util.FLogger


class MovedFocusPosAdapter(
    private val context: Context,
    private val isLeft: Boolean,
    private val favoriteTracker: RecyclerViewFocusTracker
) : SimpleBindingAdapter<ItemTelephoneFavoriteMovedBinding>() {
    private val mData = arrayListOf<Contact>()
    private var focusedState = mutableMapOf<Long, Boolean>()

    private fun Contact.isFocused(): Boolean {
        return focusedState[id] ?: false
    }

    private fun Contact.setFocused(focused: Boolean) {
        focusedState[id] = focused
    }

    /** 更新数据 */
    fun setData(data: List<Contact>) {
        mData.clear()
        mData.addAll(data)
        notifyDataSetChanged()
    }

    fun getCurrentData(): Contact? {
        val curPos = favoriteTracker.checkedSelectPos()
        if (curPos < 0 || curPos > mData.size - 1) {
            return null
        }
        return mData[curPos]
    }

    override fun onBindViewHolder(
        holder: BaseBindingHolder<ItemTelephoneFavoriteMovedBinding>,
        position: Int
    ) {
        holder.binding.apply {
            val contact = mData[position]
            if (contact == Contact.Invalid) {
                root.visibility = View.INVISIBLE
                return
            } else {
                root.visibility = View.VISIBLE
            }

            // 设置选中效果
            val isSelectedPos = favoriteTracker.checkPosSelected(position)
            FLogger.d("onBindView --> pos=$position, isSelected=$isSelectedPos")
            if (isSelectedPos) {
                root.setBackgroundResource(R.drawable.ic_tele_list_hover)
            } else {
                root.background = null
            }

            make3DEffectForSide(root, isLeft, isSelectedPos)
            tvName.text = contact.displayName
            tvPhoto.text = contact.displayName.first().toString()
            tvPhone.text = contact.phoneNum

            if (isSelectedPos) {
                if (!contact.isFocused()) {
                    startZoomWith(layoutContent, NORMAL_SIZE.first, FOCUSED_SIZE.first)
                    startZoomHeight(layoutContent, NORMAL_SIZE.second, FOCUSED_SIZE.second)
                } else {
//                    val lp = root.layoutParams
//                    lp.width = FOCUSED_SIZE.first
//                    lp.height = FOCUSED_SIZE.second
//                    layoutContent.layoutParams = lp
                }
                contact.setFocused(true)
            } else {
                if (contact.isFocused()) {
                    startZoomWith(layoutContent, FOCUSED_SIZE.first, NORMAL_SIZE.first)
                    startZoomHeight(layoutContent, FOCUSED_SIZE.second, NORMAL_SIZE.second)
                } else {
//                    val lp = layoutContent.layoutParams
//                    lp.width = NORMAL_SIZE.first
//                    lp.height = NORMAL_SIZE.second
//                    layoutContent.layoutParams = lp
                }
                contact.setFocused(false)
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    private fun startValAnim(
        from: Int,
        to: Int,
        listener: ValueAnimator.AnimatorUpdateListener?,
        duration: Long
    ) {
        if (from == to) {
            return
        }
        val animator: ValueAnimator = ValueAnimator.ofInt(from, to)
        animator.duration = duration
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener(listener)
        animator.start()
    }

    private fun <V : View?> startZoomWith(@NonNull v: V, from: Int, to: Int) {
        startValAnim(from, to, { animation ->
            val lp = v!!.layoutParams
            val size: Int = Integer.valueOf(animation.animatedValue.toString())
            lp.width = size
            v.layoutParams = lp
        }, 200)
    }

    private fun <V : View?> startZoomHeight(@NonNull v: V, from: Int, to: Int) {
        startValAnim(from, to, { animation ->
            val lp = v!!.layoutParams
            val size: Int = Integer.valueOf(animation.animatedValue.toString())
            lp.height = size
            v.layoutParams = lp
        }, 0)
    }

    companion object {
        private val NORMAL_SIZE = Pair(dp2px(400f), dp2px(82f))
        private val FOCUSED_SIZE = Pair(dp2px(426f), dp2px(82f))

        private fun dp2px(dpValue: Float): Int {
            val scale = Resources.getSystem().displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }
    }
}