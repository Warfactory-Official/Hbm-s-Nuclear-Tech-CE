#version 120

uniform sampler2D mainTex;
uniform vec4 duck;

varying vec2 pass_tex;
varying vec4 pass_color;

void main(){
	gl_FragColor = duck;
}
