#version 120

uniform sampler2D tex;
uniform sampler2D lightmap;
uniform sampler2D noise;
uniform float age;
uniform int bloom;

varying vec2 texCoord;
varying vec2 lightmapTexCoord;
varying vec3 lightSum;

void main(){
	vec4 texColor = texture2D(tex, texCoord);
	float noise_tex = texture2D(noise, texCoord).r;
	float noiseA = texture2D(noise, texCoord + vec2(age*0.01, age*0.01)).r*2;
	float noiseB = texture2D(noise, texCoord + vec2(age*2*0.01, -age*0.01)).b*2;
	float brightA = int(noise_tex < age*0.04 + 0.05)*int(noise_tex > age*0.04 - 0.05);
	float dissolve = step(age*0.04, noise_tex);
	//Rectangle
	float brightB = int(fract(noiseA) < 0.6)*int(fract(noiseA) > 0.4)*int(fract(noiseB) < 0.6)*int(fract(noiseB) > 0.4)*dissolve;
	
	//Static branching should be ok, right?
	if(bloom == 0){
		gl_FragColor = vec4(texColor.rgb * lightSum, texColor.a) * texture2D(lightmap, lightmapTexCoord) * gl_Color * dissolve
	 		+ vec4(1, 1, 1, 0)*brightA
	 		+ vec4(1, 1, 1, 0)*brightB;
	} else if(bloom == 1){
		vec3 bloomColor = vec3(0.2, 0.8, 1)*1;
		gl_FragColor = vec4(bloomColor*brightA + bloomColor*brightB, texColor.a * gl_Color.a);
	}
}
