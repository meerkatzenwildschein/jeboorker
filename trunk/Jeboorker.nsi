Name "Jeboorker Start"

# General Symbol Definitions
!define REGKEY "SOFTWARE\$(^Name)"

# Included files
!include Sections.nsh
!include FileFunc.nsh 

# Installer pages
Page instfiles

# Installer attributes
OutFile Jeboorker.exe
InstallDir $PROGRAMFILES\Jeboorker
CRCCheck on
XPStyle on
Icon "${NSISDIR}\Contrib\Graphics\Icons\classic-install.ico"
SilentInstall silent
InstallDirRegKey HKLM "${REGKEY}" Path

# Installer sections
Section -Main SEC0000
    SetOutPath $INSTDIR
    ${GetParameters} $R0
    ExecShell open "$INSTDIR\Jeboorker.vbs" $R0
SectionEnd