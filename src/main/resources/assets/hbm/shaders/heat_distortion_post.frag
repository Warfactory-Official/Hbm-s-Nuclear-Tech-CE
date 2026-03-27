#version 120

uniform sampler2D mainTex;
uniform sampler2D noise;
uniform sampler2D mc_tex;
uniform float time;
uniform vec2 windowSize;

varying vec2 tex_coord;

void main(){
	float amount = texture2D(mainTex, tex_coord).r;
	vec2 offset = texture2D(noise, tex_coord*3+time*0.3).rg*2-1;
	offset *= pow(texture2D(noise, tex_coord*0.3-time*0.05).gb*2-1, vec2(2));
	offset = offset * amount * 0.1;
	
	gl_FragColor = texture2D(mc_tex, tex_coord+offset);
}
