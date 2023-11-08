const nodefetch = require('node-fetch');
const mainURL = 'https://overpass-api.de/api/interpreter?data=';

class OverpassQuery {
	constructor() {
		this.elements1 = [];
		this.elements2 = [];
		this.elements3 = [];
		this.format = 'json';
		this.timeout = 180;
	}
	addElement1({ type, tags = [], bbox }) {
		const elementString1 = `${type}${tags.map(tag => `[${tag.key}${tag.not ? '!=' : '='}${tag.value}]`)}(area.searchArea)`;
		this.elements1.push(elementString1);
		return this;
	}
	addElement2({ type, tags = [], bbox }) {
		const elementString2 = `${type}${tags.map(tag => `[${tag.key}${tag.not ? '!=' : '='}${tag.value}]`)}(area.searchArea)`;
		this.elements2.push(elementString2);
		return this;
	}
	setFormat(format) {
		this.format = format;
		return this;
	}
	setTimeout(timeout) {
		this.timeout = timeout;
		return this;
	}
	get query() {
		return `${mainURL}[out:${this.format}][timeout:${this.timeout}];area(id:3600072596)->.searchArea;(${this.elements1.join(';')};${this.elements2.join(';')};);out body;>;out skel qt;`;
	}
	async fetch() {
		if (this.elements1.length == 0) throw new Error('No elements specified!');
		if (this.elements2.length == 0) throw new Error('No elements specified!');
		const response = await nodefetch(this.query);
		return JSON.parse(JSON.stringify(await (response).json()));
	}
}
module.exports = OverpassQuery;