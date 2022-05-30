class ToggleAction(var onOn: () -> Unit = {}, var onOff: () -> Unit = {})
{
    private var isOn = true

    fun toggle()
    {
        if (isOn) onOn()
        else onOff()

        isOn = !isOn
    }
}