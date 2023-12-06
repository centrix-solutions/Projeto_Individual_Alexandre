import com.github.britooo.looca.api.core.Looca
import org.springframework.dao.EmptyResultDataAccessException
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class Monitoramento {
    fun monitoramento() {
        val sn: Scanner = Scanner(System.`in`);
        val looca: Looca = Looca();
        val usuario:Usuario = Usuario();
        var maquina:Maquina = Maquina();
        val repositorioUsuario:UsuarioRepositorio = UsuarioRepositorio();
        val repositorioMaquina:MaquinaRepositorio = MaquinaRepositorio();
        val repositorioComponentes:ComponentesRepositorio = ComponentesRepositorio();
        Cmd.iniciar();
        repositorioUsuario.iniciar();
        repositorioMaquina.iniciar();
        repositorioComponentes.iniciar();

        println("Bem vindo ao sistema apartado da Centrix Solutions!");
        while (true){

            /* INICIO LOGIN */

            while (true){
                println("Digite o seu email:");
                val email:String = sn.nextLine();
                println("Digite a sua senha:");
                val senha:String = sn.nextLine();
                val login:Boolean = repositorioUsuario.logar(email,senha);
                if (login){
                    println("Login efetuado com sucesso!");
                    val usuarioLogado:Usuario = repositorioUsuario.buscarDados(email,senha);
                    usuario.idFuncionario = usuarioLogado.idFuncionario;
                    usuario.email = usuarioLogado.nome;
                    usuario.email = usuarioLogado.email;
                    usuario.fkEmpFunc = usuarioLogado.fkEmpFunc;
                    usuario.nome = usuarioLogado.nome;
                    println("\r\nBem vindo de volta ${usuario.nome}");
                    break;
                }else println("Email ou senha incorretos. Tente novamente.");
            }

            /* FIM LOGIN */

            /* COMEÇO VERIFICAÇÃO DE MAQUINA EXISTENTE */

            val idComputador:String = looca.processador.id;

            try {
                maquina = repositorioMaquina.verificarMaquina(idComputador);
                println("\r\nEsta maquina já foi cadastrada!");
            } catch (excecao: EmptyResultDataAccessException) {
                println("\r\nEsta maquina não existe na base de dados!");
                println("Cadastrando maquina com o monitoramento padrão");

                val novaMaquina: Maquina = Maquina();
                novaMaquina.Sistema_Operacional = looca.sistema.sistemaOperacional;
                novaMaquina.Id_do_dispositivo = idComputador;
                novaMaquina.fkEmpMaq = usuario.fkEmpFunc;
                maquina = novaMaquina;

                val maquinaCadastrada: Maquina? = repositorioMaquina.cadastrarMaquina(novaMaquina, usuario);
                if (maquinaCadastrada != null) {
                    Componentes.values().forEachIndexed() { i, it ->
                        val valor:Double = it.valor;
                        repositorioComponentes.registrarComponentes(valor, i + 1, maquinaCadastrada);
                        println("Maquina cadastrada com o monitoramento padrão!");
                    }
                }
            }

            /* FIM VERIFICAÇÃO DE MAQUINA EXISTENTE */

            /* INICIO BUSCA DE DADOS, COMPONENTES E SESSÃO */

            maquina.CPU = looca.processador.nome;
            maquina.RAM = looca.memoria.total.toDouble() / 1000000000;
            maquina.DISCO = looca.grupoDeDiscos.tamanhoTotal.toDouble() / 1000000000;

            println("Especificações do seu computador:\r\n" +
                    "ID: ${maquina.Id_do_dispositivo}\r\n" +
                    "SO: ${maquina.Sistema_Operacional}\r\n" +
                    "CPU: ${maquina.CPU}\r\n" +
                    "RAM: %.2f GB\r\n".format(maquina.RAM) +
                    "DISCO: %.2f GB".format(maquina.DISCO));

            //val monitorarProcessos:Boolean = repositorioComponentes.verificarComponenteProcesso(maquina.idMaquina);

            val horaLogin = LocalDateTime.now();
            repositorioUsuario.registrarEntrada(usuario, maquina, horaLogin);

            /* FIM BUSCA DE DADOS, COMPONENTES E SESSÃO */

            /* INICIO MONITORAMENTO */

            println("\r\nIniciando o monitoramento...\r\n");
            repositorioComponentes.limparProcessos(maquina.idMaquina);
            //if (monitorarProcessos){
            val tempo:Int = 5;
            val arquivo:String = Python.criarScript(tempo, maquina.idMaquina, usuario.fkEmpFunc);
            Python.executarScript(arquivo);
            var monitoramento:Boolean = true;
            val menu:Thread = thread {
                println("Digite...\r\n" +
                        "1. Trocar de usuário\r\n" +
                        "2. Encerrar Programa");
                val escolha:Int = sn.nextInt();
                when(escolha){
                    1 -> {
                        println("\r\nTrocando de usuário...\r\n");
                        Python.pararScript();
                        val horaLogout = LocalDateTime.now();
                        repositorioUsuario.registrarSaida(usuario, maquina, horaLogout);
                        monitoramento = false;
                    }
                    2 -> {
                        println("\r\nEncerrando o programa...\r\n");
                        Python.pararScript();
                        val horaLogout = LocalDateTime.now();
                        repositorioUsuario.registrarSaida(usuario, maquina, horaLogout);
                        repositorioComponentes.limparProcessos(maquina.idMaquina);
                        monitoramento = false;
                        exitProcess(0);
                    }
                    else -> println("Opção inválida. Por favor, escolha uma opção válida.");
                }
            }
            val monitoramentoProcesso:Thread = thread {
                while (monitoramento){
                    Cmd.buscarPid(maquina.idMaquina);
                    Thread.sleep(tempo * 1000L);
                }
            }
            menu.join()
            monitoramentoProcesso.join()

            /* FIM MONITORAMENTO */
            //}
        }
    }
}