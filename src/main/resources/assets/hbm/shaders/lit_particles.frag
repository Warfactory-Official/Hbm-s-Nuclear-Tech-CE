#version 120

varying vec2 pass_tex;
varying vec2 pass_lightmap;
varying vec4 pass_color;

uniform sampler2D mainTex;
uniform sampler2D lightmap;

void main(){
	vec4 tex = texture2D(mainTex, pass_tex);
	vec4 lmap = texture2D(lightmap, pass_lightmap);
	
	gl_FragColor = tex * lmap * pass_color;
}
