import psutil
import pygetwindow as gw
import win32gui
import win32process
import time
from mysql.connector import connect
import pymssql

local = False

if(local):
    mysql_cnx = connect(user='aluno', password='sptech', host='localhost', database='centrix')
    print("--- Local ---")
else:
    sql_server_cnx = pymssql.connect(server='44.197.21.59', database='centrix', user='sa', password='centrix')
    print("--- Server ---")
print("PIDs e títulos das janelas visíveis:")

listaAtual = []
listaTotal = []

def inserirProcesso(processoProcurado):
    #QUERY SQL
    query = (
        "INSERT INTO Processo"
        "(PID, titulo, fkCompExistentesProc, fkMaqProc, fkEmpProc)"
        "VALUES (%s, %s, %s, %s, %s)"
    )
    if(local):
    #SQL LOCAL
        bdLocal_cursor = mysql_cnx.cursor()
        bdLocal_cursor.execute(query, (processoProcurado['pid'],processoProcurado['titulo'],8,17,1))
        bdLocal_cursor.close()
        mysql_cnx.commit()
    # SQL SERVER
    else:
        bdServer_cursor = sql_server_cnx.cursor()
        bdServer_cursor.execute(query,(processoProcurado['pid'],processoProcurado['titulo'],8,17,1))
        bdServer_cursor.close()
        sql_server_cnx.commit()
    
    #print("Insert: PID:",processoProcurado['pid'],"Titulo:",processoProcurado['titulo'])
def deletarProcesso(processoProcurado):
    #QUERY SQL
    query = (
        "DELETE FROM Processo WHERE fkMaqProc = %s AND PID = %s AND titulo = %s"
    )
    if(local):
    #SQL LOCAL
        bdLocal_cursor = mysql_cnx.cursor()
        bdLocal_cursor.execute(query, (17, processoProcurado['pid'],processoProcurado['titulo']),)
        bdLocal_cursor.close()
        mysql_cnx.commit()
    else:
    # SQL SERVER
        bdServer_cursor = sql_server_cnx.cursor()
        bdServer_cursor.execute(query, (17, processoProcurado['pid'],processoProcurado['titulo']),)
        bdServer_cursor.close()
        sql_server_cnx.commit()
    
    #print("Delete: PID:",processoProcurado['pid'],"Titulo:",processoProcurado['titulo'])
def obter_pids_titulos_janelas_visiveis():
    # Obtém todas as janelas usando pygetwindow
    janelas = gw.getAllTitles()

    # Lista para armazenar os PIDs e títulos das janelas visíveis
    pids_titulos_janelas_visiveis = []

    # Exibe os PIDs e títulos das janelas visíveis
    for titulo_janela in janelas:
        # Remove espaços em branco e verifica se o título não está vazio
        titulo_limpo = titulo_janela.strip()
        if titulo_limpo:
            # Obtém o identificador da janela e o PID do processo associado
            hwnd = win32gui.FindWindow(None, titulo_janela)
            _, pid = win32process.GetWindowThreadProcessId(hwnd)

            pids_titulos_janelas_visiveis.append({"pid": pid, "titulo": titulo_limpo})
            #print(f"PID: {pid}, Título: {titulo_limpo}")

    return pids_titulos_janelas_visiveis
def obterDadosGrafico(qtd, cpu, ram):
    #QUERY SQL
    query = (
        "INSERT INTO DadosGrafico (qtdProcessos, cpu, ram, fkMaqDados, fkEmpDados) VALUES (%s, %s, %s, %s, %s)"
    )
    if(local):
    #SQL LOCAL
        bdLocal_cursor = mysql_cnx.cursor()
        bdLocal_cursor.execute(query, (qtd,cpu,ram, 17,1),)
        bdLocal_cursor.close()
        mysql_cnx.commit()
    else:
    # SQL SERVER
        bdServer_cursor = sql_server_cnx.cursor()
        bdServer_cursor.execute(query, (qtd,cpu,ram, 17,1),)
        bdServer_cursor.close()
        sql_server_cnx.commit()
if __name__ == "__main__":
    while(True):
        listaProcessos = obter_pids_titulos_janelas_visiveis()
        #na listaProcessos pega um processo e verifica se ele não está na listaAtual para adicionar na lista atual e mandar para o banco
        for processoProcurado in listaProcessos:
            if processoProcurado not in listaAtual:
                listaAtual.append(processoProcurado)
                listaTotal.append(processoProcurado)
                inserirProcesso(processoProcurado)
        #na listaAtual pega um processo e verifica se ele não está na listaProcessos (que foi pega pela função) caso não tenha é pq o processo foi fechado
        for processoProcurado in listaAtual:
            if processoProcurado not in listaProcessos:
                listaAtual.remove(processoProcurado)
                deletarProcesso(processoProcurado)
        #para a função obterDadosGrafico
        lista_processos = psutil.process_iter()
        quantidade_processos = len(list(lista_processos))
        uso_cpu = psutil.cpu_percent()
        uso_ram = psutil.virtual_memory().percent
        obterDadosGrafico(quantidade_processos, uso_cpu, uso_ram)
        time.sleep(5)
        #print(listaAtual)
        #print("")
        #print("Next:")
        #print("")