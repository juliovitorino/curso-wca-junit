package br.ce.wcaquino.entidades;

import java.util.Objects;

public class Usuario {

	private String nome;
	
	public Usuario() {}
	
	public Usuario(String nome) {
		this.nome = nome;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Override
	public boolean equals(Object obj) {
		if( this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;

		Usuario other = (Usuario) obj;
		if(nome == null && other.getNome() != null) return false;
		if(nome != null && !nome.equals(other.getNome())) return false;

		return true;

	}

	@Override
	public String toString() {
		return "Usuario{" +
				"nome='" + nome + '\'' +
				'}';
	}
}