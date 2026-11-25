!include "MUI2.nsh"
!include "nsDialogs.nsh"
!include "StrFunc.nsh"

!ifndef BUILD_UNINSTALLER
${StrStr}
Var ServidorHost
Var ServidorPorta
Var ServidorHttps
Var ServidorDiretorio
Var ServidorDialog
Var ServidorHostInput
Var ServidorPortaInput
Var ServidorHttpsInput

!macro customInit
  ; Ler antes da desinstalacao da versao anterior, inclusive em atualizacoes silenciosas.
  Call CarregarServidorInstalado
!macroend

!macro customPageAfterChangeDir
  Page custom PaginaServidor SalvarPaginaServidor
!macroend

; O builder insere customHeader depois das paginas MUI e antes de .onInit.
!macro customHeader
Function CarregarServidorInstalado
  StrCpy $ServidorHost "localhost"
  StrCpy $ServidorPorta "80"
  StrCpy $ServidorHttps "0"
  StrCpy $ServidorDiretorio "$INSTDIR"
  IfFileExists "$INSTDIR\servidor.ini" 0 servidor_carregado
    ReadINIStr $ServidorHost "$INSTDIR\servidor.ini" "servidor" "host"
    ReadINIStr $ServidorPorta "$INSTDIR\servidor.ini" "servidor" "porta"
    ReadINIStr $ServidorHttps "$INSTDIR\servidor.ini" "servidor" "https"
  servidor_carregado:
FunctionEnd

Function PaginaServidor
  ${If} $ServidorDiretorio != $INSTDIR
    Call CarregarServidorInstalado
  ${EndIf}
  !insertmacro MUI_HEADER_TEXT "Servidor do EventsByMc" "Informe onde o sistema esta sendo executado."
  nsDialogs::Create 1018
  Pop $ServidorDialog
  ${If} $ServidorDialog == error
    Abort
  ${EndIf}

  ${NSD_CreateLabel} 0 0 100% 28u "Use localhost se o servidor estiver neste computador. Em outra maquina, informe o IP do computador que hospeda o sistema."
  Pop $0
  ${NSD_CreateLabel} 0 36u 100% 12u "IP ou nome do servidor:"
  Pop $0
  ${NSD_CreateText} 0 50u 100% 13u "$ServidorHost"
  Pop $ServidorHostInput
  ${NSD_CreateLabel} 0 73u 100% 12u "Porta do site (padrao: 80):"
  Pop $0
  ${NSD_CreateText} 0 87u 75u 13u "$ServidorPorta"
  Pop $ServidorPortaInput
  ${NSD_CreateCheckbox} 0 110u 100% 14u "Usar HTTPS (servidor com certificado configurado)"
  Pop $ServidorHttpsInput
  ${NSD_SetState} $ServidorHttpsInput $ServidorHttps
  ${NSD_OnClick} $ServidorHttpsInput AlterarHttpsServidor
  ${NSD_CreateLabel} 0 136u 100% 28u "Informe a porta do site, a mesma utilizada no navegador. HTTP e permitido para localhost e IP privado da rede."
  Pop $0
  nsDialogs::Show
FunctionEnd

Function AlterarHttpsServidor
  Pop $0 ; nsDialogs envia o identificador do controle no callback.
  ${NSD_GetState} $ServidorHttpsInput $0
  ${NSD_GetText} $ServidorPortaInput $1
  ${If} $0 == ${BST_CHECKED}
  ${AndIf} $1 == "80"
    ${NSD_SetText} $ServidorPortaInput "443"
  ${ElseIf} $0 == ${BST_UNCHECKED}
  ${AndIf} $1 == "443"
    ${NSD_SetText} $ServidorPortaInput "80"
  ${EndIf}
FunctionEnd

Function SalvarPaginaServidor
  ${NSD_GetText} $ServidorHostInput $ServidorHost
  ${NSD_GetText} $ServidorPortaInput $ServidorPorta
  ${NSD_GetState} $ServidorHttpsInput $ServidorHttps
  ${If} $ServidorHttps != 0
  ${AndIf} $ServidorHttps != 1
    Goto servidor_invalido
  ${EndIf}
  StrLen $0 $ServidorHost
  ${If} $0 == 0
  ${OrIf} $0 > 253
    Goto servidor_invalido
  ${EndIf}
  StrCpy $1 0
  StrCpy $4 0 ; comprimento do rotulo DNS
  StrCpy $5 "" ; caractere anterior
  StrCpy $6 1 ; endereco composto somente por digitos e pontos
  validar_host:
    StrCpy $2 $ServidorHost 1 $1
    StrCmp $2 "" fim_host
    ${StrStr} $3 "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.-" $2
    StrCmp $3 "" servidor_invalido
    ${If} $2 == "."
      ${If} $4 == 0
      ${OrIf} $5 == "-"
        Goto servidor_invalido
      ${EndIf}
      StrCpy $4 0
    ${Else}
      ${If} $4 == 0
      ${AndIf} $2 == "-"
        Goto servidor_invalido
      ${EndIf}
      IntOp $4 $4 + 1
      IntCmp $4 63 0 0 servidor_invalido
      ${StrStr} $3 "0123456789" $2
      ${If} $3 == ""
        StrCpy $6 0
      ${EndIf}
    ${EndIf}
    StrCpy $5 $2
    IntOp $1 $1 + 1
    Goto validar_host
  fim_host:
    ${If} $4 == 0
    ${OrIf} $5 == "-"
    ${OrIf} $ServidorHost == "0.0.0.0"
      Goto servidor_invalido
    ${EndIf}
    ${If} $6 == 1
      System::Call 'ws2_32::InetPtonW(i 2, w "$ServidorHost", *i .r4) i .r5'
      IntCmp $5 1 0 servidor_invalido servidor_invalido
    ${EndIf}
    ${If} $ServidorHttps == 0
    ${AndIf} $ServidorHost != "localhost"
      ${If} $6 != 1
        Goto servidor_https
      ${EndIf}
      StrCpy $0 $ServidorHost 3
      StrCmp $0 "10." validar_porta
      StrCpy $0 $ServidorHost 4
      StrCmp $0 "127." validar_porta
      StrCpy $0 $ServidorHost 8
      StrCmp $0 "192.168." validar_porta
      StrCpy $0 $ServidorHost 4
      StrCmp $0 "172." 0 servidor_https
      StrCpy $0 $ServidorHost 1 6
      StrCmp $0 "." 0 servidor_https
      StrCpy $0 $ServidorHost 2 4
      IntCmp $0 16 0 servidor_https 0
      IntCmp $0 31 0 0 servidor_https
    ${EndIf}
  validar_porta:
    StrCmp $ServidorPorta "" servidor_invalido
    StrLen $0 $ServidorPorta
    IntCmp $0 5 0 0 servidor_invalido
    StrCpy $1 0
  validar_digitos_porta:
    StrCpy $2 $ServidorPorta 1 $1
    StrCmp $2 "" validar_intervalo_porta
    ${StrStr} $3 "0123456789" $2
    StrCmp $3 "" servidor_invalido
    IntOp $1 $1 + 1
    Goto validar_digitos_porta
  validar_intervalo_porta:
    IntCmp $ServidorPorta 1 0 servidor_invalido 0
    IntCmp $ServidorPorta 65535 0 0 servidor_invalido
    Return
  servidor_https:
    MessageBox MB_OK|MB_ICONEXCLAMATION "Para este destino, marque HTTPS. HTTP pode ser utilizado com localhost ou um IP privado da rede local."
    Abort
  servidor_invalido:
    MessageBox MB_OK|MB_ICONEXCLAMATION "Informe somente o IP ou nome do servidor (sem http://, caminhos ou espacos) e uma porta de 1 a 65535."
    Abort
FunctionEnd
!macroend

!macro customInstall
  ClearErrors
  WriteINIStr "$INSTDIR\servidor.ini" "servidor" "host" "$ServidorHost"
  WriteINIStr "$INSTDIR\servidor.ini" "servidor" "porta" "$ServidorPorta"
  WriteINIStr "$INSTDIR\servidor.ini" "servidor" "https" "$ServidorHttps"
  ${If} ${Errors}
    MessageBox MB_OK|MB_ICONSTOP "Nao foi possivel salvar a configuracao do servidor. Execute o instalador novamente."
    Abort
  ${EndIf}
!macroend
!endif
