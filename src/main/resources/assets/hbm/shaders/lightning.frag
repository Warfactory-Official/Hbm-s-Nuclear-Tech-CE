#version 120

uniform sampler2D mainTex;
uniform sampler2D noise;
uniform vec4 duck;
uniform float age;

varying vec2 pass_tex;
varying vec2 noise_tex;
varying vec4 pass_color;
varying float fade;

void main(){
	float noise = texture2D(noise, pass_tex * vec2(6, 0.25)).g;
	gl_FragColor = duck*vec4(step(fade, noise))*vec4(pass_color.rgb, step(0.5, pass_color.a));
}
