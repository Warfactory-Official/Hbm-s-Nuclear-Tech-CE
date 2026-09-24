#version 120

uniform sampler2D mainTex;

varying vec2 texCoord;

void main(){
	gl_FragColor = texture2D(mainTex, texCoord) * gl_Color;
}
