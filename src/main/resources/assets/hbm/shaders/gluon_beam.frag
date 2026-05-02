#version 120

uniform sampler2D mainTex;
uniform sampler2D noise_1;
uniform float time;
uniform float beam_length;

varying vec2 texCoord;

float remap(float num, float min1, float max1, float min2, float max2){
	return ((num - min1) / (max1 - min1)) * (max2 - min2) + min2;
}

void main(){
	vec2 coord = texCoord;
	float r = clamp(remap(beam_length*texCoord.x, 0, 0.4, 0, 1), 0.01, 1);
	coord.y = clamp((coord.y-0.5)/r+0.5, 0, 1);
	
	vec4 tex = texture2D(mainTex, coord);
	vec4 noise = texture2D(noise_1, vec2(coord.s*2-time*5, coord.t));
	vec4 tex2 = texture2D(mainTex, coord+(noise.rg-0.5)*0.02*int(tex.r < 0.2));
	
	gl_FragColor = tex2 * vec4(0.6, 0.85, 2, 1)*1.5;
	gl_FragColor = tex * vec4(0.6, 0.85, 2, 1)*1.5;
}
