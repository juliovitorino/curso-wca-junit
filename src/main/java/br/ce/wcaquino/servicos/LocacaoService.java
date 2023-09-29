package br.ce.wcaquino.servicos;

import br.ce.wcaquino.dao.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exception.FilmeSemEstoqueException;
import br.ce.wcaquino.exception.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

public class LocacaoService {

	private LocacaoDAO locacaoDAO;
	private SPCService spcService;
	private EmailService emailService;


	public LocacaoService(LocacaoDAO locacaoDAO, SPCService spcService, EmailService emailService) {
		this.locacaoDAO = locacaoDAO;
		this.spcService = spcService;
		this.emailService = emailService;
	}

	public void notificarAtrasos() {
		List<Locacao> locacoes = locacaoDAO.obterLocacoesPendentes();
		for(Locacao locacaoItem : locacoes) {
			emailService.notificarAtraso(locacaoItem.getUsuario());
		}
	}
	public Locacao alugarFilme(Usuario usuario, List<Filme> filmeList) {

		if (spcService.possuiNegativacao(usuario)) {
			throw new LocadoraException("Usuário negativado no SPC");
		}

		Double valorLocacao = getaDouble(usuario, filmeList);
		Locacao locacao = new Locacao();
		locacao.setFilme(filmeList);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(new Date());
		locacao.setValor(valorLocacao);

		//Entrega no dia seguinte
		Date dataEntrega = new Date();
		dataEntrega = adicionarDias(dataEntrega, 1);
		if(DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY)) {
			dataEntrega = DataUtils.adicionarDias(dataEntrega,1);
		} else if (DataUtils.verificarDiaSemana(dataEntrega, Calendar.SATURDAY)) {
			dataEntrega = DataUtils.adicionarDias(dataEntrega,2);
		}
		locacao.setDataRetorno(dataEntrega);

		//Salvando a locacao...
		locacaoDAO.salvar(locacao);

		return locacao;
	}

	private static Double getaDouble(Usuario usuario, List<Filme> filmeList) {
		int[] desconto = new int[]{0,0,25,50,75,100,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

		if(usuario == null) {
			throw new LocadoraException("Usuário está nulo ou vazio");
		}
		if(filmeList == null) {
			throw new LocadoraException("Filme está nulo ou vazio");
		}

		Double valorLocacao = 0.0;
		int index = 0;
		for(Filme filmeItem : filmeList) {
			if(filmeItem.getEstoque() <= 0) {
				throw new FilmeSemEstoqueException("Filme sem estoque");
			}
			int descontoAplicar = desconto[index++];
			valorLocacao += filmeItem.getPrecoLocacao() - descontoAplicar * filmeItem.getPrecoLocacao()/100;
		}
		return valorLocacao;
	}

	public static void main(String[] args) {



	}
}