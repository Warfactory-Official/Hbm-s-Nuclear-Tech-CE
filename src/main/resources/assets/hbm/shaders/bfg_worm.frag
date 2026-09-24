#version 120

uniform sampler2D mainTex;
uniform sampler2D noise;
uniform sampler2D bigNoise;
uniform float worldTime;

varying vec2 texCoord;

float remap(float num, float min1, float max1, float min2, float max2){
	return ((num - min1) / (max1 - min1)) * (max2 - min2) + min2;
}

void main(){
	float noiseA = texture2D(bigNoise, vec2(texCoord.x+worldTime/80, 0.3)).r;
	float noiseB = texture2D(bigNoise, vec2(texCoord.x-worldTime/200, 0.6)).r;
	float noiseC = texture2D(noise, vec2(fract(texCoord.x-worldTime/100)*8, 0.9)).r;
	noiseA = (noiseA + noiseB)/2;
	noiseA = remap(noiseA, 0, 1, -0.4, 0.4);
	
	float c1 = clamp(texCoord.x, 0.8, 1);
	c1 = 1-remap(c1, 0.8, 1, 0, 1);
	float c2 = clamp(texCoord.x, 0, 0.2);
	c2 = remap(c2, 0, 0.2, 0, 1);
	noiseA *= c1*c2;
	
	noiseA += remap(noiseC, 0, 1, -0.03, 0.03)*c1*c2;
	vec4 tex = texture2D(mainTex, vec2(texCoord.x, texCoord.y+noiseA));
	gl_FragColor = tex * gl_Color;
}
