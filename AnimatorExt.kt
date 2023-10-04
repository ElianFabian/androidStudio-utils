import android.animation.Animator
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun Animator.awaitAnimationStart() {
    suspendCancellableCoroutine<Unit> { continuation ->
        val listener = object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                removeListener(this)
                continuation.resume(Unit)
            }

            override fun onAnimationEnd(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}
        }

        addListener(listener)
        
        continuation.invokeOnCancellation {
            removeListener(listener)
        }
    }
}


suspend fun Animator.awaitAnimationEnd() {
    suspendCancellableCoroutine<Unit> { continuation ->
        val listener = object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                removeListener(this)
                continuation.resume(Unit)
            }

            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}
        }

        addListener(listener)

        continuation.invokeOnCancellation {
            removeListener(listener)
        }
    }
}

suspend fun Animator.awaitAnimationCancel() {
    suspendCancellableCoroutine<Unit> { continuation ->
        val listener = object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {
                removeListener(this)
                continuation.resume(Unit)
            }

            override fun onAnimationRepeat(animation: Animator?) {}
        }

        addListener(listener)
        
        continuation.invokeOnCancellation { 
            removeListener(listener)
        }
    }
}

suspend fun Animator.awaitAnimationRepeat() {
    suspendCancellableCoroutine<Unit> { continuation ->
        val listener = object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {
                removeListener(this)
                continuation.resume(Unit)
            }
        }

        addListener(listener)
        
        continuation.invokeOnCancellation { 
            removeListener(listener)
        }
    }
}

suspend fun Animator.awaitPause() {
    suspendCancellableCoroutine<Unit> { continuation ->
        val listener = object : Animator.AnimatorPauseListener {
            override fun onAnimationPause(animation: Animator?) {
                removePauseListener(this)
                continuation.resume(Unit)
            }

            override fun onAnimationResume(animation: Animator?) {}
        }

        addPauseListener(listener)
        
        continuation.invokeOnCancellation { 
            removePauseListener(listener)
        }
    }
}

suspend fun Animator.awaitResume() {
    suspendCancellableCoroutine<Unit> { continuation ->
        val listener = object : Animator.AnimatorPauseListener {
            override fun onAnimationPause(animation: Animator?) {}
            override fun onAnimationResume(animation: Animator?) {
                removePauseListener(this)
                continuation.resume(Unit)
            }
        }

        addPauseListener(listener)
        
        continuation.invokeOnCancellation { 
            removePauseListener(listener)
        }
    }
}
