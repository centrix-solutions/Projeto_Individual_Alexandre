import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UsuarioRepositorio {
    lateinit var jdbcTemplate: JdbcTemplate;

    fun iniciar() {
        if (Conexao.local) jdbcTemplate = Conexao.jdbcTemplate!! else jdbcTemplate = Conexao.jdbcTemplateServer!!;
    }
    fun logar(email:String, senha:String):Boolean {
        val consulta = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) AS count FROM Funcionario WHERE email = ? AND senha = ?;",
            arrayOf(email, senha),
            Int::class.java
        );
        return consulta == 1;
    }
    fun buscarDados(email:String, senha:String):Usuario {
        val funcionario = jdbcTemplate.queryForObject(
            "SELECT idFuncionario, email, nome, fkEmpFunc FROM Funcionario WHERE email = ? AND senha = ?",
            arrayOf(email, senha),
            BeanPropertyRowMapper(Usuario::class.java)
        );
        return funcionario;
    }
    fun registrarEntrada(usuario:Usuario, maquina:Maquina, horaLogin:LocalDateTime) {/*
        jdbcTemplate.update(
            """
        INSERT INTO Login (Email, Id_do_dispositivo, dataHoraEntrada)
        VALUES (?, ?, ?)
        """.trimIndent(),
            usuario.email,
            maquina.Id_do_dispositivo,
            horaLogin
        );*/
        jdbcTemplate.update(
            """
        INSERT INTO Login (idFuncionario, idMaquina, idEmpresa, Email, Id_do_dispositivo, dataHoraEntrada)
        VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent(),
            usuario.idFuncionario,
            maquina.idMaquina,
            usuario.fkEmpFunc,
            usuario.email,
            maquina.Id_do_dispositivo,
            horaLogin
        );
    }
    fun registrarSaida(usuarioLogado:Usuario, maquina: Maquina, horaLogout:LocalDateTime) {/*
        jdbcTemplate.update("""
            UPDATE Login
            SET dataHoraSaida = '${horaLogout.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}'
            WHERE Email = '${usuarioLogado.email}';
        """.trimIndent()
        );*/
        val ultimoLogin = jdbcTemplate.update(
            "SELECT TOP 1 idLogin FROM Login WHERE idFuncionario = ${usuarioLogado.idFuncionario} ORDER BY dataHoraEntrada DESC"
        );
        Thread.sleep(5 * 1000L)
        jdbcTemplate.update("""
            UPDATE Login
            SET dataHoraSaida = '${horaLogout.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}'
            WHERE idFuncionario = ${usuarioLogado.idFuncionario}
            AND idMaquina = ${maquina.idMaquina}
            AND idEmpresa = ${usuarioLogado.fkEmpFunc}
            AND idLogin = $ultimoLogin;
        """.trimIndent()
        );
    }
}