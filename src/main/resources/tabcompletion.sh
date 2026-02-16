#
# ▄▖    ▗ ▘▄▖▖ ▄▖
# ▌▌▌▌▀▌▜▘▌▌ ▌ ▐
# █▌▙▌█▌▐▖▌▙▖▙▖▟▖
#  ▘
#
_quati_complete() {
    mapfile -t COMPREPLY < <(compgen -W "$(quati "$COMP_POINT:$COMP_LINE")" -- "${COMP_WORDS[COMP_CWORD]}")
}
complete -F _quati_complete quati