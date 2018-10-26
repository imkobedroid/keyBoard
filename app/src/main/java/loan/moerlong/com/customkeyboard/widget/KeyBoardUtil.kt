package loan.moerlong.com.customkeyboard.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import loan.moerlong.com.customkeyboard.R
import java.lang.reflect.Method
import java.util.*

/**
 *Created by Dsh
 */
class KeyBoardUtil {

    val TAG = this.javaClass.simpleName
    var mActivity: Activity
    var mIsDecimal = false
    var mIsRandom: Boolean = false

    companion object {
        @Volatile
        var mKeyboard: Keyboard? = null
        @SuppressLint("StaticFieldLeak")
        var mKeyBoardViewContainer: View?=null
        @SuppressLint("StaticFieldLeak")
        var mEditText: EditText? = null
        lateinit var mKeyBoardView: CustomKeyboardView
        fun getKeyView(activity: Activity) {
            if (mKeyBoardViewContainer == null) {
                synchronized(View::class) {
                    if (mKeyBoardViewContainer == null) {
                        mKeyBoardViewContainer = activity.layoutInflater.inflate(R.layout.keyboardview, null)
                        mKeyBoardView = mKeyBoardViewContainer!!.findViewById(R.id.keyboard_view)
                    }
                }
            }
        }


        fun getKeyboard(activity: Activity) {
            if (mKeyboard == null) {
                synchronized(Keyboard::class) {
                    if (mKeyboard == null) {
                        mKeyboard = Keyboard(activity, R.xml.number_keyboard)
                    }
                }
            }
        }
    }


    constructor(activity: Activity) : this(activity, false, true)
    /**
     * @param activity
     * @param isRandom  是否时随机键盘
     * @param mIsDecimal  是否支持小数输入
     */
    constructor(activity: Activity, isRandom: Boolean, isDecimal: Boolean) {
        mActivity = activity
        mIsRandom = isRandom
        mIsDecimal = isDecimal
        getKeyboard(mActivity)
        getKeyView(mActivity)
        addViewToRoot()
    }


    private fun addViewToRoot() {
        val frameLayout: FrameLayout = mActivity.window.decorView.findViewById(android.R.id.content)
        val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        lp.gravity = Gravity.BOTTOM
        val viewParent: ViewParent? =mKeyBoardViewContainer!!.parent

        //判断是否操作的为同一个界面
        if (viewParent!=frameLayout){
            viewParent?.let {
                if(it is ViewGroup){
                    it.removeView(mKeyBoardViewContainer)
                }
            }
            frameLayout.addView(mKeyBoardViewContainer, lp)
        }
    }

    fun attachTo(editText: EditText) {
        //如果editText与上次设置的是同一个对象，并且键盘已经正在在显示，不再执行后续操作
        if (mEditText != null && mEditText == editText && mKeyBoardView!!.visibility == View.VISIBLE) return
        mEditText = editText
        Log.e(TAG, "attachTo")
        //根据焦点及点击监听，来显示或者隐藏键盘
        onFoucsChange()
        //隐藏系统键盘
        hideSystemSoftKeyboard()
        //显示自定义键盘
        showSoftKeyboard()
    }

    private fun onFoucsChange() {
        mEditText?.setOnFocusChangeListener { v, hasFocus ->
            //如果获取焦点，并且当前键盘没有显示，则显示，并执行动画
            if (hasFocus && mKeyBoardView.visibility != View.VISIBLE) {
                mKeyBoardView.visibility = View.VISIBLE
                startAnimation(true)
            } else if (!hasFocus && mKeyBoardView.visibility == View.VISIBLE) {
                //如果当前时失去焦点，并且当前在键盘正在显示，则隐藏
//                mKeyBoardView.visibility = View.GONE
//                startAnimation(false)
            }
        }

        mEditText?.setOnClickListener {
            //根据上面焦点的判断，如果已经获取到焦点，并且键盘隐藏。再次点击时，
            // 焦点改变函数不会回调，所以在此判断如果隐藏就显示
            if (mKeyBoardView.visibility == View.GONE) {
                mKeyBoardView.visibility = View.VISIBLE
                startAnimation(true)
            }
        }
    }

    @SuppressLint("ServiceCast")
    private fun hideSystemSoftKeyboard() {
        //11版本开始需要反射setShowSoftInputOnFocus方法设置false，来隐藏系统软键盘
        if (Build.VERSION.SDK_INT > 10) {
            var clazz = EditText::class.java
            var setShowSoftInputOnFocus: Method? = null
            setShowSoftInputOnFocus = clazz.getMethod("setShowSoftInputOnFocus", Boolean::class.java)
            setShowSoftInputOnFocus.isAccessible = true
            setShowSoftInputOnFocus.invoke(mEditText, false)
        } else {
            mEditText!!.inputType = InputType.TYPE_NULL
        }
        var inputMethodManager = mActivity.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(mEditText!!.windowToken, 0)
    }

    /*fun setOnOkClick(onOkClick: OnOkClick) {
      mOnOkClick = onOkClick
    }*/

    private fun showSoftKeyboard() {
        if (mIsRandom) {
            //生成随机键盘
            generateRandomKey()
        } else {
            //有序键盘
            mKeyBoardView.keyboard = mKeyboard
        }
        mKeyBoardView.isEnabled = true
        //设置预览，如果设置false，则就不现实预览效果
        mKeyBoardView.isPreviewEnabled = true
        //设置可见
        mKeyBoardView.visibility = View.VISIBLE
        //指定键盘弹出动画
        //startAnimation(true)
        //设置监听
        mKeyBoardView.setOnKeyboardActionListener(MyOnKeyboardActionListener())
    }

    private fun generateRandomKey() {
        var keys = mKeyboard!!.keys
        var numberKeys = mutableListOf<Keyboard.Key>()
        //保存数字
        var nums = mutableListOf<Int>()
        //0的ASCII码是48,之后顺序加1
        for (key in keys) {
            //过滤数字键盘
            if (key.label != null && "0123456789".contains(key.label)) {
                nums.add(Integer.parseInt(key.label.toString()))
                numberKeys.add(key)
            }
        }
        var random = Random()
        var changeKey = 0//更改numberKeys对应的数值
        while (nums.size > 0) {
            var size = nums.size
            var randomNum = nums[random.nextInt(size)]
            var key = numberKeys[changeKey++]
            key.codes[0] = 48 + randomNum
            key.label = randomNum.toString()
            nums.remove(randomNum)
        }
        mKeyBoardView.keyboard = mKeyboard
    }


    inner class MyOnKeyboardActionListener : KeyboardView.OnKeyboardActionListener {
        override fun swipeRight() {
            Log.e(TAG, "swipeRight")
        }

        override fun onPress(primaryCode: Int) {
            Log.e(TAG, "onPress")
            //添加震动效果
            //mActivity.applicationContext.vibrator.vibrate(50)
            ////指定隐藏（确定）删除不显示预览
            mKeyBoardView.isPreviewEnabled = !(primaryCode == Keyboard.KEYCODE_DONE || primaryCode == Keyboard.KEYCODE_DELETE)
        }

        override fun onRelease(primaryCode: Int) {
            Log.e(TAG, "onRelease")
        }

        override fun swipeLeft() {
            Log.e(TAG, "swipeLeft")
        }

        override fun swipeUp() {
            Log.e(TAG, "swipeUp")
        }

        override fun swipeDown() {
            Log.e(TAG, "swipeDown")
        }

        override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
            Log.e(TAG, "onKey primaryCode:$primaryCode keyCodes:$keyCodes")
            //if (mEditText == null) throw RuntimeException("The mEditText is null,Please call attachTo method")

            mEditText?.let {
                val editable: Editable = it.text
                val textString = editable.toString()
                //获取光标位置
                val start = it.selectionStart
                when (primaryCode) {
                    //如果是删除键，editable有值并且光标大于0（即光标之前有内容），则删除
                    Keyboard.KEYCODE_DELETE -> {
                        if (!editable.isNullOrEmpty()) {
                            if (start > 0) {
                                editable.delete(start - 1, start)
                            } else {
                            }
                        } else {
                        }
                    }
                    Keyboard.KEYCODE_DONE -> {
                        hideSoftKeyboard()
                        /*mOnOkClick?.let {
                          //点击确定时，写一个回调，如果你对有确定的需求
                          it.onOkClick()
                        }*/
                    }
                    else -> {
                        //   由于promaryCode是用的ASCII码，则直接转换字符即可，46是小数点
                        if (primaryCode != 46) {
                            //如果点击的是数字，不是小数点，则直接写入EditText,由于我codes使用的是ASCII码，
                            // 则可以直接转换为数字。当然可以你也可以获取label，或者根据你自己随便约定。
                            editable.insert(start, Character.toString(primaryCode.toChar()))
                        } else {
                            //如果点击的是逗号
                            if (mIsDecimal && primaryCode == 46) {
                                if ("" == textString) {
                                    //如果点的是小数点，并且当前无内容，自动加0
                                    editable.insert(start, "0.")
                                } else if (!textString.contains(".")) {
                                    //当前内容不含有小数点，并且光标在第一个位置，依然加0操作
                                    if (start == 0) {
                                        editable.insert(start, "0.")
                                    } else {
                                        editable.insert(start, ".")
                                    }
                                } else {
                                    //如果是不允许小数输入，或者允许小数，但是已经有小数点，则不操作
                                }
                            } else {
                            }
                        }
                    }
                }
            }
        }

        override fun onText(text: CharSequence?) {
            Log.e(TAG, "onText:" + text.toString())
        }

    }

    fun showKeyboard() {
        var visibility = mKeyBoardView.visibility
        if (visibility == View.INVISIBLE || visibility == View.GONE) {
            mKeyBoardView.visibility = View.VISIBLE
        }
    }

    fun hideSoftKeyboard(): Boolean {
        if (mEditText == null) return false
        var visibility = mKeyBoardView.visibility
        if (visibility == View.VISIBLE) {
            startAnimation(false)
            mKeyBoardView.visibility = View.GONE
            return true
        }
        return false
    }

    private fun startAnimation(isIn: Boolean) {
        Log.e(TAG, "startAnimation")
        val anim: Animation = if (isIn) {
            AnimationUtils.loadAnimation(mActivity, R.anim.anim_bottom_in)
        } else {
            AnimationUtils.loadAnimation(mActivity, R.anim.anim_bottom_out)
        }
        mKeyBoardViewContainer!!.startAnimation(anim)
    }
}