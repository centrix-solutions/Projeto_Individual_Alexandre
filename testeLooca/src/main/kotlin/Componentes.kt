import com.github.britooo.looca.api.core.Looca

enum class Componentes(val descricao:String, val valor:Double) {
    CPU("Cpu",100.0),
    DISCO("Disco",Looca().grupoDeDiscos.tamanhoTotal.toDouble() / 1000000000),
    RAM("Ram",Looca().memoria.total.toDouble() / 1000000000),
    USB("Usb",Looca().dispositivosUsbGrupo.totalDispositvosUsbConectados.toDouble()),
    TAXA_DOWNLOAD("Taxa Download",0.0),
    TAXA_UPLOAD("Taxa Upload",0.0),
    JANELAS_DO_SISTEMA("Janelas do Sistema",0.0),
    PROCESSOS("Processos",0.0);
}